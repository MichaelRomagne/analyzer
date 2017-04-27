%define __jar_repack %{nil}

Name:		moogsoft
Version:
Release:
Source0:	%{name}-%{version}.tar.gz
License:	Moogsoft EULA - http://www.moogsoft.com/eula
Group:		Networking
Summary:	Situational Service Management Software
Packager:	Robert Harper <rob@moogsoft.com>
Distribution:	Moogsoft
Vendor:		Moogsoft Ltd.
URL:		http://www.moogsoft.com
BuildRoot:	%(mktemp -ud %{_tmppath}/%{name}-%{version}-XXXXXX)

Requires:	libkqueue = 1.0.2
Requires:	libgfortran >= 4.4.6
Requires:	jdk1.8.0_20 = 2000:1.8.0_20-fcs
Requires:	gnustep-make = 2.6.1
Requires:	gnustep-base = 1.22.1
Requires:	apache-tomcat = 7.0.55
Requires:	tomcat-native = 1.1.30
Requires:	apache-cassandra = 1.0.5
Requires:	libmicrohttpd-devel >= 0.9.0
Requires:	mysql >= 5.5.24
Requires:	mysql-server >= 5.5.24
Requires:	mysql-libs  >= 5.5.24
Requires:	php >= 5.3.13
Requires:	php-cli >= 5.3.13
Requires:	php-common >= 5.3.13
Requires:	php-mysqlnd >= 5.3.13
Requires:	php-pdo >= 5.3.13
Requires:	httpd >= 2.2.15
Requires:	mod_ssl >= 2.2.15
Requires:	openssl >= 1.0.0
Requires:	sphinx >= 2.0.7
Requires:	pcre >= 8.13
Requires:	moogsoft-eula >= 4.1.1

Prefix:				%{_datadir}
Prefix:				%{_var}/run
Prefix:				%{_var}/log
Prefix:				%{_var}/lib
Prefix:				%{_sysconfdir}

%description

%prep
%setup -n  %{name}-%{version}
#stop 'make' on the servlets from triggering a 'make install'
%{__sed} -ie "s/^DELIVER_CMD=\$(INSTALL).*//" $RPM_BUILD_DIR/%{name}-%{version}/src-utils/compile/rules/javaservlet.mk
#use the right sphinx configuration for Linux
%{__mv} $RPM_BUILD_DIR/%{name}-%{version}/common/config/sphinx/linux_sphinx.conf $RPM_BUILD_DIR/%{name}-%{version}/common/config/sphinx/sphinx.conf


%build
export MOOGSOFT_HOME="$RPM_BUILD_DIR"/%{name}-%{version}/build
export MOOGSOFT_SRC_HOME="$RPM_BUILD_DIR"/%{name}-%{version}
export MOOGSOFT_ROOT="$RPM_BUILD_DIR"/%{name}-%{version}
export LD_LIBRARY_PATH="$MOOGSOFT_HOME"/lib:"$LD_LIBRARY_PATH"
export MOOGSOFT_COTS_HOME=%{_cots}

%{__make}


%install
# Roll out moogsoft into phoney %{_datadir}/moogsoft
%{__mkdir_p} %{buildroot}/%{_datadir}/moogsoft
%{__cp} -Rip ./build/{bin,bots,config,etc,lib,ui} %{buildroot}/%{_datadir}/moogsoft

# These folders are put into the right location by Jenkins
%{__cp} -Rip ./docs %{buildroot}/%{_datadir}/moogsoft
%{__cp} -Rip ./schema_upgrades %{buildroot}/%{_datadir}/moogsoft/etc/moog
%{__cp} -Rip ./VERSION.txt %{buildroot}/%{_datadir}/moogsoft/etc
%{__cp} -Rip ./ui/eula.txt %{buildroot}/%{_datadir}/moogsoft/ui

# create init.d moog service log/pid directories and change owner to moogsoft
%{__mkdir_p} %{buildroot}%{_sysconfdir}/init.d
%{__mkdir_p} %{buildroot}%{_var}/log/moogsoft
%{__mkdir_p} %{buildroot}%{_var}/run/moogsoft
%{__mkdir_p} %{buildroot}%{_var}/lib/moogsoft
%{__mkdir_p} %{buildroot}%{_var}/lib/moogsoft/moog-data

%{__cp} -ip ./build/etc/service-wrappers/moomsd %{buildroot}/%{_sysconfdir}/init.d
%{__cp} -ip ./build/etc/service-wrappers/moogfarmd %{buildroot}/%{_sysconfdir}/init.d
%{__cp} -ip ./build/etc/service-wrappers/socketlamd %{buildroot}/%{_sysconfdir}/init.d
%{__cp} -ip ./build/etc/service-wrappers/trapdlamd %{buildroot}/%{_sysconfdir}/init.d

# Take care of tomcat stuff
%{__mkdir_p} %{buildroot}/%{_datadir}/apache-tomcat/webapps
%{__mkdir_p} %{buildroot}/%{_datadir}/apache-tomcat/lib
%{__cp} -ip ./build/lib/moogsvr.war %{buildroot}/%{_datadir}/apache-tomcat/webapps
%{__cp} -ip ./build/lib/moogpoller.war %{buildroot}/%{_datadir}/apache-tomcat/webapps
%{__cp} -ip ./build/lib/toolrunner.war %{buildroot}/%{_datadir}/apache-tomcat/webapps
%{__cp} -ip ./build/lib/graze.war %{buildroot}/%{_datadir}/apache-tomcat/webapps
%{__cp} -ip ./build/lib/cots/mysql-connector-java-5.1.17-bin.jar %{buildroot}/%{_datadir}/apache-tomcat/lib

%clean
# Just nuke everything
rm -rf %{buildroot}

%pre -p /bin/sh

eula_file="${RPM_INSTALL_PREFIX0}/moogsoft/etc/eula/accepted-incident-eula.txt"
eula_file_text="I_ACCEPT_EULA"

if [ ! -f "$eula_file" ]
then
   echo
   echo "Cancelling install."
   echo "Incident.MOOG EULA acceptance file, $eula_file, not found."
   echo "Please install or reinstall the moogsoft-eula package."
   exit 1
fi

if [ `grep -c "${eula_file_text}" "$eula_file"` -eq "0" ]
then
   echo
   echo "Cancelling install."
   echo "Invalid Incident.MOOG EULA acceptance file, $eula_file."
   echo "Please install or reinstall the moogsoft-eula package."
   exit 1
fi

# Add moog user & group
getent group moogsoft > /dev/null || groupadd -r moogsoft
getent passwd moogsoft > /dev/null || useradd -r -g moogsoft moogsoft
getent passwd moogadmin > /dev/null || useradd -g moogsoft moogadmin
usermod -G apache moogadmin

%post

%preun
# Shutdown services and remove them from all runlevels
# This is "ify" section, if service gets removed manually %preun scriptlet
# will fail and cause failure of the package removal

# if [ "$1" = "0" ] ; then
# service moomsd stop > /dev/null 2>&1
# chkconfig --del moomsd
# fi

# MySQL
if [ "$1" = "0" ] ; then
service mysqld stop > /dev/null 2>&1
chkconfig --del mysqld
fi

# ApacheWebServer
if [ "$1" = "0" ] ; then
service httpd stop > /dev/null 2>&1
chkconfig --del httpd
fi

# ApacheTomcat
if [ "$1" = "0" ] ; then
service apache-tomcat stop > /dev/null 2>&1
chkconfig --del apache-tomcat
fi

# ApacheCassandra
if [ "$1" = "0" ] ; then
service cassandra stop > /dev/null 2>&1
chkconfig --del cassandra
fi

%files
%defattr(-,moogsoft,moogsoft,-)
%dir %{_datadir}/moogsoft
%dir %{_datadir}/moogsoft/ui
%dir %{_datadir}/moogsoft/bots
%dir %{_datadir}/moogsoft/docs

# optionally we can use here: %config(noreplace)
%config %attr(775,moogsoft,moogsoft) %{_datadir}/moogsoft/config
%config %attr(775,moogsoft,moogsoft) %{_datadir}/moogsoft/bots/lambots
%config %attr(775,moogsoft,moogsoft) %{_datadir}/moogsoft/bots/moobots

# init scripts (moogsoft daemons)
%config %attr(775,moogsoft,moogsoft) %{_sysconfdir}/init.d/moomsd
%config %attr(775,moogsoft,moogsoft) %{_sysconfdir}/init.d/moogfarmd
%config %attr(775,moogsoft,moogsoft) %{_sysconfdir}/init.d/socketlamd
%config %attr(775,moogsoft,moogsoft) %{_sysconfdir}/init.d/trapdlamd

%{_var}/log/moogsoft
%{_var}/run/moogsoft
%{_datadir}/moogsoft/bin
%config %attr(775,moogsoft,moogsoft) %{_datadir}/moogsoft%{_sysconfdir}
%{_datadir}/moogsoft/lib

%{_datadir}/moogsoft/ui/js
%{_datadir}/moogsoft/ui/lib
%{_datadir}/moogsoft/ui/resources

%{_datadir}/moogsoft/docs/release_notes

# this is just preliminary file attribute settings - needs revisiting
%attr(664,apache,apache) %{_datadir}/moogsoft/ui/index.html
%attr(664,apache,apache) %{_datadir}/moogsoft/ui/eula.txt
%attr(-,apache,apache) %{_datadir}/moogsoft/ui/css
%attr(-,apache,apache) %{_datadir}/moogsoft/ui/downloads
%attr(775,apache,apache) %{_datadir}/moogsoft/ui/html
%attr(-,apache,apache) %{_datadir}/moogsoft/ui/images

%attr(-,tomcat,tomcat) %{_datadir}/apache-tomcat/webapps/moogsvr.war
%attr(-,tomcat,tomcat) %{_datadir}/apache-tomcat/webapps/moogpoller.war
%attr(-,tomcat,tomcat) %{_datadir}/apache-tomcat/webapps/toolrunner.war
%attr(-,tomcat,tomcat) %{_datadir}/apache-tomcat/webapps/graze.war
%attr(-,tomcat,tomcat) %{_datadir}/apache-tomcat/lib/mysql-connector-java-5.1.17-bin.jar

%attr(775,moogsoft,moogsoft) %{_var}/log/moogsoft
%attr(775,moogsoft,moogsoft) %{_var}/run/moogsoft
%attr(-,moogsoft,moogsoft) %{_var}/lib/moogsoft
%attr(-,tomcat,tomcat) %{_var}/lib/moogsoft/moog-data
