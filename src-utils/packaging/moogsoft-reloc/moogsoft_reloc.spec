%define _use_internal_dependency_generator 0

Summary:	Moogsoft Runtime Environment
Name:		moogsoft_reloc
Version:	
Release:	
License:	distributable
Group:		Virtual groups/Moogsoft Runtime Dependencies
Source0:	%{name}-%{version}-%{release}.tar.gz


# mogsoft nonrelocatable dependencies
Requires:   libgfortran		>= 4.4.6
Requires:   libkqueue		= 1.0.2
Requires:   jdk1.8.0_20     = 2000:1.8.0_20-fcs
Requires:   gnustep-make	= 2.6.1
Requires:   gnustep-base	= 1.22.1
Requires:   rpm-libs		>= 4.8.0
Requires:   php				>= 5.3.13
Requires:   php-cli			>= 5.3.13
Requires:   php-common		>= 5.3.13
Requires:   php-mysqlnd		>= 5.3.13
Requires:   php-pdo			>= 5.3.13
Requires:   httpd			>= 2.2.15
Requires:   mod_ssl			>= 2.2.15
Requires:   openssl			>= 1.0.0

# apache-cassandra = 1.0.5 requires
Requires:   jpackage-utils	>= 1.7.5
Requires:   shadow-utils	>= 4.1.4.2

# mysql-libs  >= 5.5.24 requires
Requires:   glibc			>= 2.12
Requires:   libgcc			>= 4.4.6
Requires:   libstdc++		>= 4.4.6
Requires:   zlib			>= 1.2.3

# mysql >= 5.5.24 requires
Requires:   ncurses-libs	>= 5.7
Requires:   perl			>= 5.10.1
Requires:   perl-DBI		>= 1.609
Requires:   perl-DBD-MySQL	>= 4.013

# mysql-server >= 5.5.24 requires
Requires:   chkconfig		>= 1.3.49.3
Requires:   initscripts		>= 9.03.27
Requires:   libaio			>= 0.3.107

# sphinx >= 2.0.7 requires
Requires:   expat			>= 2.0.1
Requires:   unixODBC		>= 2.2.14
Requires:   postgresql-libs	>= 8.4.13

BuildArch		:	noarch
Prefix			:	/usr/share/moogsoft-install
BuildArch		:	noarch

%description
Moogsoft-deps is  meta-package, it clears the path for "moogsoft" installation by making sure that all non-relocatable dependencies are installed. Also deliversrelocation scripts into default location /usr/share/moogsoft-install

%prep
%setup -n  %{name}-%{version}-%{release}

%build

%clean 

%pre
getent group moogsoft > /dev/null || groupadd -r moogsoft
getent passwd moogsoft > /dev/null || useradd -r -g moogsoft moogsoft

%install
%{__mkdir_p} %{buildroot}/usr/share/moogsoft-install/scripts
%{__cp} -Rip {mysql,tomcat,cassandra,sphinx,moogsoft} %{buildroot}/usr/share/moogsoft-install/scripts
%{__cp} {bashrc_template,install_deps,main.conf,install_deps_scripts,myservices} %{buildroot}/usr/share/moogsoft-install/scripts

%post
# remove self (bordercollie)after required packages are installed.
# rpm -e %{name}

%preun

%postun

%files 
%defattr(-,moogsoft,moogsoft,-)
%dir /usr/share/moogsoft-install/scripts
%config /usr/share/moogsoft-install/scripts
# /usr/share/moogsoft-install/scripts/cassandra
# /usr/share/moogsoft-install/scripts/tomcat
# /usr/share/moogsoft-install/scripts/sphinx
# /usr/share/moogsoft-install/scripts/mysql
# /usr/share/moogsoft-install/scripts/moogsoft




%changelog
* Thu Jul 11 2013 Predrag Mutavdzic <fred@moogsoft.com>
- Initial build of the meta-packege moogsoft-deps
