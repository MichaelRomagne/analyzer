%define __jar_repack %{nil}

Name:		moogsoft-eula	
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
BuildArch:	noarch
BuildRoot:	%(mktemp -ud %{_tmppath}/%{name}-%{version}-XXXXXX)

Prefix:		%{_datadir}

Requires:	/bin/sh

%description
Configures system with Moogsoft EULA file

%prep

%setup -n  %{name}-%{version}

# Confirm eula package installed and accepted
%pre -p /bin/sh

SUCCESS=0
FAILURE=1
has_valid_eula=$FAILURE
if [ -e "${RPM_INSTALL_PREFIX0}/moogsoft/etc/eula/accepted-incident-eula.txt" ] ; then
	rm -f ${RPM_INSTALL_PREFIX0}/moogsoft/etc/eula/accepted-incident-eula.txt
	
	#Code commented out in case of future pre-check requirements
	#grep -q "I_ACCEPT_EULA" "${RPM_INSTALL_PREFIX0}/moogsoft/etc/eula/accepted-incident-eula.txt"
	#if [ $? -eq $SUCCESS ] ; then
	#	has_valid_eula=$SUCCESS
	#fi
fi


if [ $has_valid_eula -ne $SUCCESS ] ; then

   # send output from file descriptor 6 to stdout
   exec 6>&1
   # stdout replaced with /dev/tty. All output send to /dev/tty.
   exec>/dev/tty
   export LESS_IS_MORE=1
   export LESSSECURE=1
   less --QUIT-AT-EOF <<"EOF"



MOOGSOFT CLICKWRAP SUBSCRIPTION LICENSE & SUPPORT AGREEMENT FOR INCIDENT.MOOG



This License agreement is between you (as an individual installing the software and also the organisation on whose behalf the individual is acting) ("you", "Customer") and either Moogsoft Inc (if you are a non-European entity) or Moogsoft Limited (if you are a European entity) (in either case, "Moogsoft").  

IMPORTANT: BY ELECTING TO INSTALL THE MOOG SOFTWARE OR BY HAVING THE MOOG SOFTWARE INSTALLED ON YOUR COMPUTER SYSTEM BY MOOGSOFT OR A THIRD PARTY (WHICH WILL BE DEEMED TO ACT AS YOUR AGENT WITH YOUR EXPRESS CONSENT), YOU AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. HOWEVER, IF YOU HAVE SIGNED A SEPARATE AGREEMENT WITH MOOGSOFT FOR THE MOOG SOFTWARE, THEN THAT AGREEMENT SHALL APPLY TO THE EXCLUSION OF THIS ONE EXCEPT THAT THE SUPPLEMENTARY PRODUCT TERMS MOOG.MARKET CONFIGURATION MODULES WILL APPLY IN ANY EVENT.
IF THE MOOG SOFTWARE IS BEING MADE AVAILABLE TO YOU FOR EVALUATION OR PROOF OF CONCEPT PURPOSES, PLEASE SEE CLAUSE 2 OF THIS AGREEMENT.
IF YOU DO NOT WISH TO ACCEPT THE TERMS OF THIS AGREEMENT AS STATED, THEN MOOGSOFT IS UNWILLING TO LICENSE THE MOOG SOFTWARE TO YOU; IN WHICH EVENT, YOU MUST TYPE "NO" WHEN PROMPTED OR EXIT THE INSTALLER USING 'CTRL-C'.  YOU SHOULD THEN RETURN ANY AND ALL SOFTWARE MEDIA AND RELATED MATERIALS TO MOOGSOFT AND ENSURE THAT ANY COPIES OF THE MOOG SOFTWARE ARE PERMANENTLY DELETED FROM YOUR COMPUTER SYSTEM.
If you are accepting this License on behalf of a firm, organisation or corporate entity, you warrant and represent to Moogsoft that you have the authority to do so. If you are not so authorized, you assume sole personal liability for the obligations set out in this Agreement.

Moogsoft reserves the right, at its sole discretion, to change, modify, add or remove portions of this Agreement at any time, for any reason. We will notify you of any material changes by posting the amended Agreement on the Moogsoft Website at www.Moogsoft.com/EULA. You must review this Agreement on a regular basis to keep yourself apprised of any changes. If you do not agree to the revised terms and conditions, your sole recourse is to immediately stop using the Moog Software. Your continued use of the Moog Software following any changes to this Agreement indicates your acceptance and agreement to any and all such changes.

This is a License agreement and not an agreement for sale. All rights not specifically granted in this License are reserved to Moogsoft and its third party licensor(s).	

1. INTERPRETATION

1.1. In this Agreement, save where the context requires otherwise, the following words and expressions have the following meaning:
"Agreement" means this Subscription License and Support Agreement between Moogsoft and Customer;
"Business Day" means Monday to Friday, excluding bank and public holidays in California (USA) or England (UK) as applicable; 
"Customer Data" means all data, information and documentation belonging to Customer or its licensors to which Moogsoft is granted access and provided a license to use for the purposes of this Agreement;
"Commencement Date" means the date that the Licensed Software is delivered to Customer; 
"Confidential Information" is confidential or proprietary information including without limitation designs, drawings, data (including Customer Data), processes, logic, logic diagrams, flow charts, coding sheets, coding, source or object code, listings, test data, test routines, diagnostic programs, ideas, concepts, know-how, intellectual property (including the Moog Software), plans, specifications, customer details and other technical, financial or commercial information, and all notes, records and copies of such information, (whether disclosed  before, on or after the date of this Agreement and whether in oral, documentary or whatever form or on whatever media or by way of models or by demonstrations) which is disclosed by or on behalf of one party to the other pursuant to this Agreement;
"Contract Year" means each period of one (1) year commencing, as the case may be, on the Commencement Date, and each anniversary thereof;
"Documentation" means the user manuals relating to the Moog Software;
"Enterprise License" (if applicable) means the right for an unlimited number of Customer's and its Subsidiaries' Managed Entities or Users (as agreed in writing by Moogsoft) to access and use the Licensed Software in accordance with the terms of this Agreement;
"Evaluation Fee" means the fee (if any) payable by Customer to Moogsoft in connection with the evaluation of the Licensed Software as detailed in clause 2;
"Fees" means the Subscription Fees, Installation Fee (if any), Evaluation Fee (if any), and any other fees payable to Moogsoft under this Agreement;
"Installation Services" means the services provided by Moogsoft to Customer in connection with the configuration, delivery and implementation of the Licensed Software as may be agreed between the parties and set out in a Statement of Work;
"Installation Fee" means the fee for carrying out the Installation Services as stated in the Statement of Work or other order document;
"Initial Term" means the term as stated in the order document or Statement Of Work signed by Customer if appropriate or, if no such term is stated therein, the period of one (1) year from the Commencement Date; 
"Intellectual Property Rights" means all existing and future intellectual property rights in and to the Moog Software, updates, upgrades, Moogsoft's Confidential Information, and other Moogsoft products and services, including, but not limited to, patents, copyrights, inventions, applications, registrations, authors' rights, moral rights, contract and licensing rights, trademarks, tradenames, database rights, domain names, service marks, know-how and trade secrets, all customizations, configurations, modifications and derivatives thereof, and other intellectual property rights (whether registered or unregistered) and all applications and registrations for any extensions or renewals of such rights or any of them, anywhere in the world;
"License" means the evaluation License (as detailed in clause 2), Standard License or Enterprise License (as applicable and as agreed in writing by Moogsoft) to use the Licensed Software granted by Moogsoft to Customer pursuant to this Agreement;
"Licensed Software" means the Moog Software (including, for Standard Licenses and Enterprise Licenses, all bug fixes, error correction and enhancements thereto made generally available by Moogsoft pursuant to the Support Services), as agreed in writing by Moogsoft, and for which Customer has paid the relevant Subscription Fees;
"License Type" means the license type (being either an evaluation License (as detailed in clause 2), Standard License or Enterprise License) as agreed in writing by Moogsoft;
"Location" means the Customer location at which the Services will be provided by Moogsoft, as may be detailed in the Statement of Work;
"Managed Entities" means any monitored component capable of producing a unique stream of telemetry messages and creating its own discrete event or log message feed such as servers, applications, network devices, routers and databases; 
"Moog Software" means Moogsoft's proprietary software (including all bug fixes, error corrections and enhancements thereto);
"Normal Working Hours" means the hours of 9.00 a.m. to 5.00 p.m.  Monday to Friday, excluding bank and public holidays in California, US (if you are a non-European entity) or the UK (if you are a European entity); 
"Services" means the Installation Services identified in a Statement of Work to be provided by Moogsoft to Customer, and the Support Services to be provided by Moogsoft to Customer under this Agreement, as applicable;
"Standard License" (if applicable) means a License for the number of Managed Entities or Users as agreed in writing by Moogsoft  to use the Licensed Software in accordance with this Agreement;
"Statement of Work" or "SOW" means the document agreed between the parties detailing the Installation Services and which sets out the key stages and dates for delivery and installation of the Licensed Software;
"Subscription Fees" means the License fees payable by Customer in connection with a Standard License or Enterprise License and Support Services;
"Subsidiary" means any corporation of which more than fifty percent (50%) of the voting stock is directly or indirectly owned or controlled by Customer, and "Subsidiaries" shall be construed accordingly;
"Support Contact" means the Customer's nominated contact person or persons through whom all support issues are raised to Moogsoft, as agreed between the parties;
"Support Services" means Moogsoft's standard support services for the Licensed Software; and
"User" means an individual who is granted access to the Licensed Software, subject to this Agreement.

2. PROOF OF CONCEPT AND EVALUATION LICENSES

2.1. This clause 2 shall apply if the Licensed Software has been made available to Customer by Moogsoft for proof of concept or evaluation purposes. In the event of conflict, the terms and conditions of this clause 2 shall prevail.
2.2. This clause shall override clauses 3 and 7.1. Subject to payment of the Evaluation Fee (if any), Moogsoft grants to Customer a limited non-exclusive, non-transferable License, in object code form only for Customer to use the Licensed Software solely for the internal purpose of evaluating and testing the Licensed Software for suitability with Customer's application. Such License shall begin on the Commencement Date and shall continue for the time period agreed in writing by Moogsoft or, if no such time period has been agreed, such Evaluation Period shall continue for a fixed period of thirty (30) days from the Commencement Date ("Evaluation Period"). Such License is granted subject to Customer's compliance with the relevant terms of this Agreement.
2.3. This clause shall override clause 8.1. Upon the Commencement Date Customer shall pay to Moogsoft all Evaluation Fees (if any).
2.4. This clause shall override clause 9.1.2. Customer acknowledges and agrees that the Licensed Software (including Documentation) is provided hereunder "as is", and Moogsoft makes, and Customer receives, no warranties in connection with the Licensed Software and/or Documentation, express, implied, statutory or otherwise, and Moogsoft specifically disclaims all implied warranties, including warranties of accuracy/reliability, merchantability, non-infringement and fitness for a particular purpose, or arising from a course of dealing, usage or trade practice.
2.5. This clause shall override clause 7.4.7. Customer shall not make available the Licensed Software in whole or in part to any third party otherwise than in accordance with this Agreement.
2.6. At the end of the Evaluation Period Customer will promptly notify Moogsoft in writing whether it wishes to purchase a License for the Licensed Software.  If Customer does not wish to purchase a License for the Licensed Software it shall return it in accordance with clause 16.2.
2.7. The provisions of clauses 6.2 and 7.6 shall not apply to evaluation Licenses granted pursuant to this clause 2.
2.8. To the extent Customer invents or develops any intellectual property in connection with using, testing or evaluating the Licensed Software, those intellectual property rights shall be owned by Moogsoft.

3. COMMENCEMENT AND DURATION

3.1. This Agreement shall commence on the Commencement Date and, subject to clause 15, shall continue for the Initial Term.
3.2. Subject to clause 15, following the expiration of the Initial Term, this Agreement shall automatically renew for additional one (1) year terms subject to the Customer paying Moogsoft's Subscription Fee for the License and Support Service (as detailed in clause 8.5 or 8.6 as applicable) unless or until terminated by either party giving notice in writing to the other of not less than one (1) month prior to the end of a Contract Year.   

4. MOOGSOFT OBLIGATIONS

4.1. Subject to payment by Customer of the applicable Fees, Moogsoft shall supply the Services and license the Licensed Software using the reasonable skill and care expected of a competent provider of such software and services, and in accordance with this Agreement.
4.2. Moogsoft shall ensure that the Licensed Software has been scanned for viruses and other malicious threats using generally available, up to date virus checking software prior to providing the Licensed Software to Customer.

5. CUSTOMER OBLIGATIONS

5.1. Customer shall:
5.1.1. promptly provide such assistance, facilities, equipment, information and documents, and grant access to computer systems, software and premises, as Moogsoft may reasonably request from time to time in order to discharge its obligations under this Agreement, and notify Moogsoft promptly of any changes in Customer's working practices, equipment or circumstances that may have an impact on Moogsoft's ability to perform the Services;
5.1.2.  take all reasonable precautions to protect the health and safety of Moogsoft's personnel, agents and sub-contractors while at the offices or facilities of Customer, and use the Licensed Software and Services in accordance with all applicable laws and regulations and the terms of this Agreement;
5.1.3. ensure that its employees and other independent contractors provide reasonable co-operation to Moogsoft in relation to the provision of the Licensed Software, supply of the Services and performance of this Agreement, and ensure that all such personnel shall have the requisite skill, qualification and experience to perform the tasks assigned to them; and
5.1.4. ensure the adequacy, integrity, security, virus checking and accuracy of Customer Data and its computer systems and operate all necessary back-up procedures to ensure the same are maintained in the event of loss for any reason. 

6. SERVICES

6.1. Installation Services:
6.1.1. Subject to payment of the Installation Fee Moogsoft will perform the Installation Services in accordance with the relevant Statement of Work and this Agreement. 
6.1.2. If the Installation Services are delayed at the request of, or because of the acts or omissions of, Customer, the parties shall: 
6.1.2.1. amend the Statement of Work to take account of such delay; and/or
6.1.2.2. increase the Installation Fee by an amount not exceeding the increase in cost to Moogsoft of carrying out its obligations under the Statement of Work and this Agreement.
6.1.3. Subject to clauses 6.1.2 and 6.1.4, if the Installation Services are delayed because of the acts or omissions of Moogsoft, Moogsoft shall, at no cost to Customer, take all necessary steps reasonably available to it (including increasing the number of its staff assigned to the performance of this Agreement), to reduce such delay and to meet the timetable in the Statement of Work.  
6.1.4. Moogsoft shall be given an extension of the timetable of any one or more of the stages in the Statement of Work if one of more of the following events occurs: 
6.1.4.1. a variation to the Statement of Work is made at Customer's request;
6.1.4.2. a force majeure event occurs as described in clause 18; or
6.1.4.3. a delay is caused in whole or in part by an action or omission of Customer, its employees, agents or third party contractors.
6.1.5. If either party is aware of any delay in the delivery of the Installation Services, it shall raise the issue as soon as reasonably practicable and the parties shall use all reasonable efforts to agree in writing the appropriate course of action including, potentially and without limitation, amendments to the timetable in the Statement of Work or the Installation Fee.
6.1.6. Any Licensed Software and Services delivery dates are estimates only and Moogsoft shall not be liable for any loss, cost, expenses or damages suffered by the Customer or a third party, however arising and whether directly or indirectly, from the failure of Moogsoft to comply with a particular date.
6.1.7. Each of the parties shall appoint a project manager to manage and coordinate the Installation Services. Neither party shall change its project manager without prior notice to the other.
6.1.8. During the course of this Agreement, the parties' project managers and other representatives shall meet at regular intervals (to be agreed between the parties) to discuss the progress of the Installation Services, and any other matters arising under this Agreement.
6.2. Support Services:
6.2.1. Subject to payment of the Subscription Fee during each Contract Year, Moogsoft will provide Support Services to Customer until termination or expiration of the Agreement in accordance with clauses 3.1 or 15.

7. SOFTWARE LICENSE

7.1. Beginning on the Commencement Date and subject to payment of the Subscription Fees, Moogsoft hereby grants, and Customer accepts, a non-exclusive, non-transferable License to use the Licensed Software, in object code form only, in accordance with the License Type agreed in writing by Moogsoft and subject to the terms of this Agreement.
7.2. The License and the use of the Licensed Software is subject to the restrictions contained in this Agreement. 
7.3. The Licensed Software may be:
7.3.1. used by the number of Users or Managed Devices (as applicable) as agreed in writing by Moogsoft;
7.3.2. copied as necessary only for backup purposes provided that Customer maintains an up to date written or electronic record of the number of copies of the Licensed Software and their location and upon request promptly delivers such record to Moogsoft. Such copies of the Licensed Software shall be subject to the terms of this Agreement.
7.4. Customer shall not:
7.4.1. duplicate, reverse engineer, decompile, reverse compile, disassemble, record or otherwise reproduce or attempt to reproduce any part of the Licensed Software, nor otherwise reduce any part of the Licensed Software to human-readable form, nor attempt to do any of the foregoing, or instruct any third party to attempt to do any of the foregoing (except and only to the extent as may be otherwise permitted by applicable law for interoperability purposes);
7.4.2. use the Licensed Software otherwise than in connection with the internal business functions of Customer;
7.4.3. allow the Licensed Software to be used by, or for the benefit of, any person other than an employee or contractor of Customer as permitted by the License except with the prior written consent of Moogsoft;
7.4.4. assign, sub-license, transfer, sell, lease, rent, charge, lend, time-share or otherwise deal in or encumber the Licensed Software (whether in whole or in part);
7.4.5. remove, obscure or alter any copyright or other proprietary notice on the Licensed Software, nor permit a third party to do so;
7.4.6. copy, make error corrections to, or otherwise modify, adapt or translate the Licensed Software, nor create derivative works based upon the Licensed Software, nor permit a third party to do so;
7.4.7. make available the Licensed Software in whole or in part to any third party otherwise than in accordance with this Agreement except that, during the term of the License, Customer may permit its third party consultants and contractors to access and use the Licensed Software  solely for the benefit of Customer and Customer's internal business functions. Any such use by Customer's third party consultants and contractors shall count towards the number of Licensed Users or Managed Devices agreed in writing by Moogsoft. Customer shall be responsible for compliance by such consultants or contractors with the terms of the License and shall be liable to Moogsoft for breach of the License by such parties. Customer shall require such consultants and contractors to discontinue use of, and access to, the Licensed Software upon completion of the work for Customer;
7.4.8. either directly or indirectly engage in any form of commercial exploitation of the Moog Software. "Commercial exploitation" for the purposes of this clause means allowing third parties access to the Moog Software (except as provided under this Agreement) and/or to services provided through use of the Licensed Software, regardless of whether revenue is generated by Customer;
7.4.9. attempt to disable or circumvent any of the licensing mechanisms within the Moog Software if they are present.
7.5. The Customer shall:
7.5.1. use all reasonable efforts to protect the Moog Software from any use, reproduction, exploitation, distribution, or publication not specifically permitted under this Agreement;
7.5.2. erase the Licensed Software from all hardware prior to disposing of or retiring such hardware from active use or in the event of termination of this Agreement, and Customer must also destroy all other copies (including those referred to in clause 7.4.7) upon such termination;
7.5.3. reproduce on any copy (whether in machine readable or human readable form) of the Licensed Software Moogsoft's copyright or trademarks notices;
7.5.4. notify Moogsoft immediately if Customer becomes aware of any unauthorized use of the whole or part of the Licensed Software by any third party; and
7.5.5. without prejudice to the foregoing take all such steps as shall from time to time be necessary to protect the Confidential Information and Intellectual Property Rights of Moogsoft.
7.6. Customer shall install and use the most recent versions of the Licensed Software made generally available by Moogsoft as part of the Support Services as soon as reasonably practicable after their release. Customer understands and acknowledges that it is necessary for the Customer to ensure that all such bug fixes, error corrections and enhancements are implemented by Customer in order for Moogsoft to properly provide the Support Services. 
7.7. Customer acknowledges that the License is subject to compliance with any and all applicable United States, UK and international laws, regulations, or orders relating to the export of computer software or related know-how ("Export Laws"). Customer agrees that the Moog Software will not be shipped, transferred or exported into any country or used in any manner prohibited by the Export Laws. In addition, if the Moog Software is identified as export controlled items under the Export Laws, Customer represents and warrants that it is not a citizen of, or otherwise located within, an embargoed nation and that it is not otherwise prohibited under the Export Laws from receiving the Moog Software. All rights to use the Moog Software are granted on condition that such rights are forfeited if Customer fails to comply with the terms of the License and in particular this clause 7.7.
7.8. A breach by Customer of any of the restrictions in this clause or in clause 11 shall, for the purposes of clause 15.1.2, be deemed to be a material breach of this Agreement which is not capable of being remedied.
7.9. Upon at least five (5) Business Days' written notice, Moogsoft (or Moogsoft's appointed third party representative) may audit the computer systems and records of Customer, its Subsidiaries, and any third parties to whom access to the software is granted pursuant to clause 7.4.7, for the purpose of ascertaining whether Customer is complying with the terms of the License granted pursuant to this Agreement.  Customer shall, and shall ensure that its Subsidiaries and such third parties shall, provide to Moogsoft all reasonable co-operation and assistance in relation to the audit which shall be conducted at Moogsoft's expense, provided however that if Customer is found to be in breach of this Agreement Customer shall pay Moogsoft's costs for conducting the audit and any additional Subscription Fees that are found to be due as a result of such audit.  This clause shall survive the termination of this Agreement and shall continue for a period of twelve (12) months following expiration or termination of this Agreement. If the underpaid Subscription Fees exceed 5% of the Subscription Fees paid, then Customer shall also pay Moogsoft's reasonable costs of conducting the audit.
7.10. Customer has the option to access and use certain Licensed Software provided to Customer by Moogsoft under this Agreement which is subject to supplementary terms and conditions.  These terms and conditions are shown below at the end of this Agreement (the "Supplementary Product Terms").  To the extent that there is any conflict or inconsistency between the Supplementary Product Terms and the other terms and conditions of this Agreement with respect to an item of Licensed Software that is subject to such Supplementary Product Terms, the Supplementary Product Terms shall prevail and govern with respect to such item of Licensed Software.

8.  FEES

8.1. Beginning on the Commencement Date and throughout the term of this Agreement Customer shall pay (i) the Installation Fees in the Statement Of Work (if any);  and (ii) annual Subscription Fees at Moogsoft's then-current rates until termination of the Agreement in accordance with clauses 3.1 or 15. Customer shall pay the Subscription Fees annually in advance. 
8.2. Customer shall pay to Moogsoft any sum due to Moogsoft under this Agreement within thirty (30) days of receipt of Moogsoft's invoice detailing such sum unless otherwise agreed in writing by the parties. 
8.3. All sums due to Moogsoft under this Agreement are exclusive of any state or federal sales or other taxes which, if applicable, shall be shown separately on its invoices and which shall be paid by Customer to Moogsoft at the prevailing rate at the same time as payment of the invoice (but shall not include and Customer shall not be charged for Moogsoft's income taxes). 
8.4. If any sum is not paid on the due date, Moogsoft may (without prejudice to any right or remedy available to it):
8.4.1. charge interest at a daily rate of three percent (3%) above the LIBOR rate (if Customer is a non-European entity) or the Barclays Bank base rate (if Customer is a European entity) and such interest shall run from day to day and accrue after as well as before any judgment and shall from time to time be compounded monthly on the amount overdue until payment thereof;
8.4.2. suspend the performance of its obligations under this Agreement until payment in full if such sum remains outstanding for a further period of fourteen (14) days. 
8.5. Renewal Standard License: Following the Initial Term and throughout the remaining term of this Agreement the Customer shall pay the annual Subscription Fees, which shall be calculated by increasing the Subscription Fees by five percent (5%) or (a) the US Consumer Price Index (US CPI) (if Customer is a non-European entity); or (b) the UK Consumer Price Index (UK CPI) (if Customer is a European entity); (whichever is the greater) per annum (compounded) until termination of the Agreement in accordance with clauses 3.2 or 15.  Customer shall pay the Subscription Fees in advance. 
8.6. Renewal Enterprise License: at the end of the Initial Term the Customer's Enterprise License will cease and the Customer will inform Moogsoft of the number of Users and Managed Entities being used at that time. Subject to the Customer's continued compliance with this Agreement, Moogsoft will grant to Customer an annual Standard License for the reported number of Users and Managed Entities at the then current list price ("Renewal Subscription Fees").  
8.7. At any time upon written request a Standard Licence for additional Users and Managed Entities may be added subject to payment of additional Subscription Fees, which shall be calculated at the then current list price and applied on a pro rata basis.  
8.8. Future annual Subscription Fees for any Standard Licences granted by Moogsoft pursuant to clause 8.6 and 8.7 will be calculated in accordance with 8.5 above.  Customer shall pay the Renewal Subscription Fees and all subsequent Subscription Fees in advance. 
8.9. At its' sole discretion Moogsoft (or Moogsoft's appointed third party representative) may choose to audit the computer systems and records of the Customer in accordance with clause 7.9 to confirm the accuracy of the number of Users and Managed Entities reported by Customer under 8.6 above. 

9. WARRANTY

9.1. Moogsoft undertakes with and warrants to Customer that:
9.1.1. the Services will be supplied with reasonable skill and care;
9.1.2. the Licensed Software at the Commencement Date, and for three (3) months after that date, will perform in all material respects in accordance with the Documentation.  Customer's sole remedy for a breach of the warranty under this clause shall be to require Moogsoft to correct the Licensed Software, within a reasonable time, so that it complies with this warranty; and
9.1.3. Moogsoft has all necessary rights to enter into and perform the terms of this Agreement.
9.2. Due to the nature of computer programs, no guarantee is given of uninterrupted or error-free running or that all errors will be rectified by error correction or avoidance action.
9.3. Moogsoft shall not be liable for a breach of this Agreement or any of the warranties in clause 9 to the extent that the breach arises:
9.3.1. because Customer failed to follow Moogsoft's written instructions as to the storage, installation, hosting, commissioning, use or maintenance of the Licensed Software; 
9.3.2. because Customer has altered or repaired the Licensed Software without the prior written consent of Moogsoft; or 
9.3.3. due to wilful damage, negligence or misuse or abuse of the Licensed Software by Customer or its employees, agents or sub-contractors.
9.4. Customer acknowledges that the Moog Software may be incompatible with all or part of the third party software or hardware used by Customer.
9.5. EXCEPT TO THE EXTENT EXPRESSLY SET FORTH IN THIS AGREEMENT, MOOGSOFT MAKES NO WARRANTIES, REPRESENTATIONS OR ENDORSEMENTS, OF ANY KIND, WHETHER EXPRESS, IMPLIED, STATUTORY OR OTHERWISE, WITH RESPECT TO THIS AGREEMENT, THE MOOG SOFTWARE, ANY SERVICES TO BE PROVIDED BY MOOGSOFT UNDER THIS AGREEMENT, OR ANY OTHER RELATED INFORMATION OR MATERIALS FURNISHED TO CUSTOMER UNDER THIS AGREEMENT, AND EXPRESSLY DISCLAIMS ALL OTHER CONDITIONS, WARRANTIES, TERMS, UNDERTAKINGS, REPRESENTATIONS, OR OTHER STATEMENTS WHATSOEVER TO THE FULLEST EXTENT PERMISSIBLE BY LAW, WHETHER EXPRESS OR IMPLIED, WRITTEN OR ORAL, BY STATUTE OR OTHERWISE, INCLUDING ANY IMPLIED WARRANTIES OF TITLE, NONINFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
9.6. The United Nations Convention on Contracts for the International Sale of Goods is expressly excluded if and to the extent that it applies to this Agreement.

10. LIABILITY

10.1. Nothing in this Agreement shall exclude or restrict Moogsoft's liability for:
10.1.1. death or personal injury resulting from its negligence; or
10.1.2. fraudulent misrepresentation.
10.2. Notwithstanding anything to the contrary in this Agreement, Moogsoft's aggregate liability (including any liability for the acts and omissions of its employees, agents and subcontractors) to Customer in any Contract Year whether arising in contract, tort or otherwise under or in connection with this Agreement shall be limited to an amount equal to the total payments paid by Customer to Moogsoft under this Agreement in that Contract Year. 
10.3. Moogsoft shall not in any event be liable to Customer under this Agreement in contract, tort (including negligence) or otherwise for:
10.3.1. loss of revenue, loss of business contracts, loss of anticipated savings or loss of profits or depletion of goodwill or similar losses (whether direct or indirect);
10.3.2.  Special, indirect or consequential loss or damage (not falling within clause 10.3.1); and
10.3.3. Loss of data, including without limitation Customer Data. 
10.4. Moogsoft shall not in any event be liable to Customer under this Agreement in contract, tort (including negligence) or otherwise or be deemed to be in breach of its obligations under this Agreement: 
10.4.1. for any delay in performing or failure to perform Moogsoft's obligations to the extent that such delay or failure was due to a failure by Customer to perform its obligations under this Agreement; or  
10.4.2. for the consequences of any acts or omissions of Customer or its employees, contractors or agents.
10.5.  Notwithstanding anything to the contrary in this Agreement, Moogsoft accepts no responsibility or liability for any third party software or elements which may be provided under the Agreement, such software or elements being the responsibility of the relevant third party licensor.
10.6. Customer agrees that a breach of this Agreement may not be adequately compensated by damages alone; Moogsoft shall be entitled to seek the remedies of injunction, specific performance or any other equitable relief for any actual, threatened or potential breach.

11. PROPRIETARY RIGHTS 

11.1. All Intellectual Property Rights and confidential information contained in and relating to the Moog Software and Services are and shall remain the exclusive property of Moogsoft and/or its licensors as appropriate. 
11.2. Certain software code incorporated into or distributed with the Evaluation Product may be licensed by third parties under various “open-source” or “public-source” software licenses  (collectively, the “Open Source Software”).  Notwithstanding anything to the contrary in this Agreement, the Open Source Software is not subject to this Agreement and instead is separately licensed pursuant to the terms and conditions of their respective open-source software licenses, either attached to this Agreement, reproduced in the appropriate readme file, or available on the Supplier web site at www.moogsoft.com/OpenSourceLicenses.  Customer agrees to comply with the terms and conditions of such open-source software license agreements
11.3. Moogsoft may develop updates to its Intellectual Property Rights at any time including in the course of or as a result of providing the Licensed Software and Services to Customer (including the demonstration of Moog Software to the Customer), provided that the development of such updates shall not compromise the Customer's rights in or to the Customer Data, nor shall it create any license to the Customer Data from the Customer to Moogsoft other than as provided in this Agreement. Nothing in this Agreement is intended to transfer ownership in and relating to any materials, processes or concepts owned by Customer (including the Customer Data) which have been provided to Moogsoft for the purposes of supplying the Moog Software and Services..
11.4. Notwithstanding anything to the contrary in this Agreement, Moogsoft shall not be prohibited or enjoined at any time from utilizing any skills or knowledge of a general nature acquired during the course of providing the Support Services or any other services, including (but not limited to) information publicly known or available or information that could reasonably be acquired during similar work for another customer of Moogsoft. In order to prevent unauthorized use of Moogsoft's Confidential Information and Intellectual Property Rights in and relating to the Moog Software, and in addition to all other rights of Moogsoft and obligations of Customer herein, Customer agrees not to develop, or instruct any third party to develop on its behalf, any software which displays the same or similar functions as the Moog Software during this Agreement and for a period of twelve (12) months following its termination.  Nothing in this clause shall prevent Customer from using or customizing any software product which is generally available in the market (during this Agreement or at its termination) as an alternative to the Moog Software provided always that Customer does not disclose any of Moogsoft's Confidential Information or Intellectual Property Rights to the supplier of such software product.

12. CONFIDENTIALITY

12.1. Without prejudice to clause 11, Moogsoft and Customer agree (subject to clauses 12.2 and 12.4) not to:
12.1.1. disclose any Confidential Information which it receives from the other party and which is identified as confidential or proprietary by the other party or the nature of which is clearly or should reasonably be considered confidential or proprietary; 
12.1.2. make any use of any such Confidential Information other than for the purpose of performance of this Agreement.
12.2. Each party may disclose Confidential Information received from the other to its responsible employees, consultants, sub-contractors or suppliers who need to receive the information in the course of performance of this Agreement and who have entered into an agreement containing confidentiality provisions no less onerous than those contained in this Agreement.
12.3. Each party shall protect and safeguard Confidential Information received from the other to the same degree as it would in respect of its own Confidential Information and in any event shall apply a reasonable standard of care in protecting and safeguarding such Confidential Information.
12.4. The confidentiality obligations in this clause shall not apply to any information which:
12.4.1. is or subsequently becomes available to the general public other than through a breach by the receiving party; or
12.4.2. is demonstrably already known to the receiving party before disclosure by the disclosing party; or
12.4.3. the receiving party rightfully receives from a third party without restriction as to use.
12.5. Upon termination or expiration of this Agreement, upon the written request of the other party, each party agrees that it shall return or destroy all Confidential Information obtained from the other party and all copies thereof. Notwithstanding such destruction or return, the obligations of confidentiality in relation to such Confidential Information shall survive termination or expiry of the Agreement. Nothing in this clause shall effect the obligations outlined in 16.2 below with regard to the Licensed Software.

13. INTELLECTUAL PROPERTY RIGHTS INDEMNITY

13.1. Provided always that Customer shall mitigate such damages, costs and expenses to the fullest extent reasonably possible, Moogsoft shall indemnify and hold Customer harmless against any reasonable costs (including legal costs), expenses or damages suffered or incurred by Customer arising out of any claim by a third party that the normal operation, possession or use of the Moog Software by Customer in accordance with this Agreement infringes the copyright or patent rights or trade secrets of such third party, provided that Customer:
13.1.1. gives Moogsoft prompt, written notice of any such claim;
13.1.2. gives Moogsoft control of the defense and/or settlement of such claim and Customer has not taken any action that would compromise Moogsoft's ability to settle or otherwise defend the claim; and
13.1.3. shall reasonably co-operate in any defense or settlement.
13.2. In the event that a court of competent jurisdiction upholds a claim of infringement as set out in clause 13.1 or if in Moogsoft's reasonable opinion such claim is likely to be upheld, Moogsoft shall at its option and expense:
13.2.1. obtain for Customer the right to continue using the Licensed Software; or
13.2.2. replace or modify the Licensed Software without materially impairing the operation of the Licensed Software so its use becomes non infringing or otherwise lawful; or
13.2.3. if either of the above cannot be accomplished on reasonable terms (and for the avoidance of doubt such failure shall not constitute a breach of this Agreement), remove the Licensed Software and refund to Customer the total amount paid by Customer under this Agreement for the Licensed Software less a reasonable sum in respect of Customer's use of the Licensed Software to the date of such removal.
13.3. Clauses 13.1 and 13.2 state Moogsoft's entire liability for infringement of third party rights or trade secrets misappropriation.
13.4. Notwithstanding the foregoing, Moogsoft shall have no liability for any claim of patent or copyright infringement or trade secret misappropriation based upon, or caused by, the operation or use of the Moog Software:
13.4.1. in breach of this Agreement;
13.4.2. on a computer operating system for which it was not designed;
13.4.3. with any other software not supplied by Moogsoft;
13.4.4. in any other manner or purpose for which the Moog Software was not designed or recommended by Moogsoft;
13.4.5. with any data or materials which have been input into the Moog Software by Customer or at Customer's direction;
13.4.6. with any processes or concepts owned or used by Customer (whether combined with the Moog Software or otherwise);  
13.4.7. if the infringement or misappropriation would have been avoided by Customer's use of the most current version of the Moog Software; or
13.4.8. which has been modified by anyone other than Moogsoft.

14. NON-SOLICITATION

14.1. Customer shall not (except with the prior written consent of Moogsoft) at any time during the term of this Agreement or for a further period of twelve (12) months after the termination of this Agreement directly or indirectly solicit or entice away (or attempt to solicit or entice away) from the employment of Moogsoft any person employed or engaged by Moogsoft in the performance of this Agreement other than by means of a national advertising campaign open to all comers and not specifically targeted at any of Moogsoft's employees.

15. TERMINATION

15.1. Notwithstanding clause 3, this Agreement may be terminated at any time by notice in writing having immediate effect in any of the following events:
15.1.1. by Moogsoft, if Customer fails to pay any sum payable under this Agreement when due and such sum remains outstanding more than fifteen (15) days after notice from Moogsoft to Customer requiring it to be paid;
15.1.2. by either party, if the other commits any material breach of this Agreement and, in the case of a material breach capable of remedy, fails to remedy the same within thirty (30) days after receipt of a written notice giving particulars of the breach and requiring it to be remedied;
15.1.3. by either party, if the other (i) passes a resolution for winding up (other than for the purposes of solvent amalgamation or reconstruction where the resulting entity is at least as creditworthy as the original entity and assumes all of the obligations of the original entity under this Agreement) or a court shall make an order to that effect; or (ii) ceases to carry on business or substantially the whole of its business; or (iii) becomes or is declared insolvent, or convenes a meeting of or makes or proposes to make any arrangement or composition with its creditors; or (iv) if a liquidator, receiver, administrator, administrative receiver, manager, trustee, or similar officer is appointed over any of its assets or (v) in the event of institution of bankruptcy, receivership, insolvency, reorganization, or other similar proceedings by or against the other party under any section or chapter of the United States Bankruptcy Code, as amended, or under any similar laws or statutes of the United States or any state thereof, if such proceedings have not been dismissed or discharged within thirty (30) calendar days after they are instituted; or the insolvency or making of an assignment for the benefit of creditors or the admittance by either party of any involuntary debts as they mature or the institution of any reorganization arrangement or other readjustment of debt plan of either party not involving the United States Bankruptcy Code.

16. CONSEQUENCES OF TERMINATION

16.1. The License shall cease upon termination (however occurring) of this Agreement.
16.2. Within seven (7) days of termination of the License, Customer shall return to Moogsoft the Licensed Software together with a certificate signed by a duly authorized representative of Customer that any copies of the Licensed Software not returned have been destroyed.
16.3. Termination shall be in addition to, rather than a waiver of, any remedy at law or equity under this Agreement.
16.4. Notwithstanding the foregoing, the provisions of clauses 7.9 (Audit) 10 ("Liability"), 11 ("Proprietary Rights") 12 ("Confidentiality") 13 ("Intellectual Property Rights"), 14 ("Non-Solicitation"), 16 ("Consequences of Termination") and 20 ("Dispute Resolution and Governing Law") shall survive termination.

17. ASSIGNMENT

17.1. This Agreement may not be assigned by either party without the prior written approval of the other party and any purported assignment in violation of this section shall be void; provided, however, that either party may assign this Agreement in connection with the transfer, directly or indirectly, of more than fifty percent (50%) of such party's outstanding voting securities or of all or substantially all of the assets or business of such party (a "Change in Control"); provided, further, that Moogsoft may assign this Agreement to any of its affiliates. Upon any assignment of this Agreement by Customer in connection with a Change in Control, any licenses that contain an "unlimited" feature will, with respect to Customer or the successor entity, as applicable, be capped at the number of authorized software units in use immediately prior to such Change in Control.  Moogsoft may subcontract the performance of any of its obligations under this Agreement to any third party, but such subcontracting shall not relieve Moogsoft of any liability under this Agreement.

18. FORCE MAJEURE

18.1. Notwithstanding anything contained in this Agreement if total or partial performance hereof is delayed or rendered impossible for Moogsoft by virtue of any reason whatsoever beyond its reasonable control (including but not limited to decision of any court or other judicial body of competent jurisdiction, unavailability of equipment, power or other commodity, failure or non availability of Internet or telecommunications facilities, acts of government or other prevailing authorities or defaults of third parties) then such non performance will not be deemed to constitute a breach by Moogsoft of this Agreement and Moogsoft shall not be liable for any loss or damage which Customer may suffer as a result.  

19. NOTICES

19.1. Any notice or report shall be considered given if delivered personally or if sent by first class mail, postage prepaid, to either party at the address shown on www.moogsoft.com for the contacting Moogsoft entity and on the order document for Customer and addressed to the Chief Executive Officer (or equivalent) of the relevant party. 
19.2. The parties may change such addresses by providing notice to the other in accordance with this clause.
19.3. Notice delivered personally shall be deemed served when delivered. Notice given by post in accordance with this clause shall be deemed served two Business Days after the date on which it is postmarked and sent.

20. DISPUTE RESOLUTION & GOVERNING LAW

20.1. The parties agree that if any controversy or claim arises in relation to this Agreement, representatives of each party shall negotiate promptly and in good faith in an attempt to resolve the matter between themselves.
20.2. If having followed the processes set out in clause 20.1, the parties have failed to resolve their controversy or settle their claim, then the matter shall be determined as follows:
20.2.1. If the parties cannot informally settle any claim or controversy arising out of this Agreement, then the parties will submit the dispute to non-binding mediation by a mutually acceptable mediator to be chosen by the parties within 45 days after written notice by either party to the other demanding mediation. Neither party may unreasonably withhold consent as to the selection of a mediator and the parties will share the cost of mediation equally. Any mediation will be held in California, USA (where Customer is a non-European entity), or in the UK (where Customer is a European entity). 
20.2.2. Where Customer is a non-European entity, this Agreement shall be governed by and construed in accordance with the laws of the State of California without giving effect to its principles of conflict of laws.  Any dispute not resolved by the above procedures shall be litigated in the state or federal courts located in San Francisco, California to whose exclusive jurisdiction the parties hereby consent. For purposes of establishing jurisdiction in California under this Agreement, each party hereby waives, to the fullest extent permitted by applicable law, any claim that:  (i) it is not personally subject to the jurisdiction of such court; (ii) it is immune from any legal process with respect to it or its property; and (iii) any such suit, action or proceeding is brought in an inconvenient forum
20.2.3. Where Customer is a European entity, this Agreement shall be governed by and construed in accordance with the laws of England. Any dispute not resolved by the above procedures shall be litigated in the courts of England to whose exclusive jurisdiction the parties hereby consent.

21. PUBLICITY

21.1. The Customer agrees to allow Moogsoft to use the Customer's name and logo in Moogsoft's company marketing materials for business purposes.
21.2. Subject to a review process before completion, the Customer agrees to:
21.2.1. act as a reference site for Moogsoft, whereupon Moogsoft will coordinate with the Customer with respect to the timing of any related calls or site visits by third parties with the Customer, and
21.2.2. provide a testimonial and a quote to Moogsoft for use in its marketing materials, including Moogsoft website and a news release by Moogsoft that Customer is a Moogsoft customer.

22. GENERAL

22.1. A person who is not a party to this Agreement has no right to enforce any term of this Agreement.
22.2. Except as may be detailed in the Agreement, no modifications to this Agreement shall be binding upon either party unless made in writing executed by an authorized representative of Moogsoft and Customer.
22.3. If either party fails to perform any of its obligations under this Agreement and the other party fails to enforce the provisions relating thereto, such party's failure to enforce this Agreement shall not prevent its later enforcement.
22.4. If any provision of this Agreement is held invalid, illegal or unenforceable, that provision shall be construed so as to most closely reflect the original intent of the parties, but still be enforceable, and the remaining provisions shall continue in full force and effect.
22.5. Where a provision of this Agreement states that written consent is required, such written consent may be given by an authorized representative of the relevant party in written electronic format (such as by email.)


SUPPLEMENTARY PRODUCT TERMS - MOOG.MARKET CONFIGURATION MODULES


As referred to in Section 7.10 above Customer may choose to access the moog.Market configuration modules under this Agreement.  If Customer chooses to access to any of the moog.Market configuration modules, the following supplementary terms and conditions (these "Supplementary Product Terms") shall apply with respect to the moog.Market configuration modules and Customer's use and distribution thereof.  These Supplementary Product Terms form a part of, and (except to the extent expressly set forth to the contrary below) apply in addition to, the other terms and conditions set forth in the main body of the Agreement.
  
1.	DEFINITIONS

1.1	"Configuration Modules" means, collectively, (a) Moogsoft's proprietary moog.Market configuration modules in source code format, and (b) any modifications or derivative works of such modules created by Customer.
1.2	"Device" means an endpoint on a network, where the Moogsoft Software has been deployed on such network.
1.3	"Module Documentation" means the end-user documentation for the Configuration Modules supplied by Moogsoft to Customer.
1.4	"Other Moogsoft Software" means any of the Licensed Software other than the Configuration Modules.

2.	LICENSE

2.1	License Grant.  Subject to the terms and conditions of this Agreement, Moogsoft hereby grants to Customer a limited, nontransferable license (without the right to sublicense), during the term of this Agreement, to (a) install, reproduce, and use the Configuration Modules on Devices solely to permit such Devices to communicate with the Other Moogsoft Software; (b) reproduce, modify, and create derivative works of the Configuration Modules to facilitate the communication of the Devices with the Other Moogsoft Software; (c) distribute the Configuration Modules to third parties solely as embedded in one or more Devices; and (d) reproduce a reasonable number of copies of the Module Documentation for its own internal-use purposes.
2.2	Restrictions.  Customer agrees and acknowledges that the Configuration Modules and Module Documentation contain valuable trade secrets of Moogsoft.  Notwithstanding anything to the contrary in this Agreement, Customer shall not (a) use, reproduce, modify, distribute, or otherwise exploit the Configuration Modules other than in connection with the licensed use of the Other Moogsoft Software; (b) distribute, share, license, resell, or otherwise transfer the Configuration Modules to any third party other than as embedded in (and solely for use with) a Device communicating with the Other Moogsoft Software; (c) attempt to circumvent any license key, disabling code, or license management mechanism in the Configuration Modules; (d) export or re-export the Configuration Modules or Module Documentation in violation of any applicable law or regulation; (e) use the Configuration Modules or Module Documentation for service-bureau use or otherwise for the benefit of third parties; or (f) otherwise exceed the scope of the express licenses granted herein.  Customer may not remove or alter any of the trademarks, trade names, logos, patent or copyright notices or markings set forth in the Configuration Modules or Module Documentation, or add any other notices or markings to the Configuration Modules or Module Documentation.  
2.3	Distribution.  If Customer distributes the Configuration Modules to any third party, it shall do so subject to a written license agreement containing terms and conditions that are no less protective of Moogsoft's rights in the Configuration Modules than the terms and conditions of this Agreement (including these Supplementary Product Terms).
2.4	Open-Source Software.  Certain software code incorporated into or distributed with the Configuration Modules may be licensed by third parties under various "open-source" or "public-source" software licenses (such as the GNU General Public License, the GNU Lesser General Public License, the Apache License, the Berkeley Software Distribution License, and the Sun Public License) (collectively, the "Open Source Software").  Notwithstanding anything to the contrary in this Agreement, the Open Source Software is not licensed under Section 2.1 of these Supplementary Product Terms and instead is separately licensed pursuant to the terms and conditions of their respective open-source software licenses, copies of which are either attached to this Agreement, reproduced in the appropriate readme file, or available on the Moogsoft web site.  Customer agrees to comply with the terms and conditions of such open-source software license agreements.
2.5	Ownership.  As between Moogsoft and Customer, Moogsoft shall own all right, title, and interest in and to the Configuration Modules (including any modifications, adaptations, or derivative works that may be created by Customer hereunder) and Module Documentation, and all Intellectual Property Rights related thereto.  To the extent that Customer obtains any ownership interest in the Configuration Modules or Module Documentation, Customer hereby irrevocably assigns all right, title, and interest in the Configuration Modules and Module Documentation (and all Intellectual Property Rights related thereto) to Moogsoft.  If Customer has any right to the Configuration Modules or Module Documentation that cannot be assigned to Moogsoft by Customer, Customer unconditionally and irrevocably grants to Moogsoft during the term of such rights, an exclusive, even as to Customer, irrevocable, perpetual, worldwide, fully paid and royalty-free license, with rights to sublicense through multiple levels of sublicensees, to reproduce, make derivative works of, distribute, publicly perform and publicly display in any form or medium, whether now known or later developed, make, use, sell, import, offer for sale and exercise any and all such rights.  If Customer has any rights to the Configuration Modules or Module Documentation that cannot be assigned or licensed to Moogsoft, Customer unconditionally and irrevocably waives the enforcement of such rights, and all claims and causes of action of any kind against Moogsoft or related to Moogsoft's customers, with respect to such rights, and agrees, at Moogsoft's request and expense, to consent to and join in any action to enforce such rights.  The Configuration Modules and Module Documentation are licensed, not sold, by Moogsoft to Customer.  There are no implied licenses granted under this Agreement, and all rights not expressly granted are reserved.
2.6	Proof of Concept and Evaluation Licenses.  Notwithstanding anything to the contrary in this Agreement (including these Supplementary Product Terms), if Customer has licensed the Other Moogsoft Software under a proof-of-concept or an evaluation license (in other words, such that Section 2 of the main body of the Agreement applies to Customer's use of the Other Moogsoft Software), then (a) Customer may exercise the license rights set forth in Section 2.1 of these Supplementary Product Terms solely for the internal purpose of evaluating and testing the Licensed Software for suitability with Customer's application; and (b) Customer may not distribute the Configuration Modules to any third party.  If Customer wishes to use the Configuration Modules or Module Documentation for any other purpose, Customer must first purchase a Standard License or Enterprise License for the Other Moogsoft Software.
2.7	Clarification.  The terms and conditions set forth in this Section 2 of these Supplementary Product Terms shall apply with respect to the Configuration Modules and the Module Documentation in lieu of Sections 2, 7.1, 7.3, 7.4, and 7.6 of the main body of the Agreement.  However, Sections 7.2, 7.5, 7.7, 7.8, 7.9, and 7.10 of the main body of the Agreement (together with the other sections of the main body of the Agreement not expressly excluded in these Supplementary Product Terms) shall continue to apply to the Configuration Modules and the Module Documentation.

3.	WARRANTY DISCLAIMERS

THE CONFIGURATION MODULES AND MODULE DOCUMENTATION ARE PROVIDED ON AN "AS IS" BASIS WITHOUT ANY SUPPORT OR WARRANTY OF ANY KIND.  AS A RESULT, SECTIONS 4, 6, 9, AND 13 OF THE MAIN BODY OF THE AGREEMENT SHALL NOT APPLY TO THE CONFIGURATION MODULES OR THE MODULE DOCUMENTATION.  IN ADDITION, WITHOUT LIMITING THE FOREGOING, MOOGSOFT MAKES NO WARRANTIES, REPRESENTATIONS OR ENDORSEMENTS, OF ANY KIND, WHETHER EXPRESS, IMPLIED, STATUTORY OR OTHERWISE, WITH RESPECT TO THE CONFIGURATION MODULES THE MODULE DOCUMENTATION, OR ANY OTHER RELATED INFORMATION OR MATERIALS FURNISHED TO CUSTOMER UNDER THIS AGREEMENT, AND EXPRESSLY DISCLAIMS ANY IMPLIED WARRANTIES OF TITLE, NONINFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  MOOGSOFT DOES NOT WARRANT THAT THE CONFIGURATION MODULES OR MODULE DOCUMENTATION ARE ERROR-FREE, THAT THEY WILL OPERATE IN CONJUNCTION WITH CUSTOMER'S HARDWARE OR NETWORK WITHOUT IMPAIRMENT OF FUNCTIONALITY, OR THAT THEY WILL DELIVER ANY PARTICULAR RESULTS.

4.	INDEMNIFICATION

Customer shall defend, hold harmless and indemnify Moogsoft from and against any Losses incurred by Moogsoft in connection with any claim or action brought against Moogsoft by a third party arising from or relating to any modification, adaptation, or derivative work of the Configuration Modules created by Customer.  Moogsoft shall (a) give Customer prompt notice of any such claim or action; (b) allow Customer to control the defense of any applicable claim or action, except that Customer may not settle any claim or action affecting Moogsoft's interests without Moogsoft's consent; and (c) provide reasonable assistance and information to Customer, at Customer's expense, for the defense of the claim.  

5.	TERM; TERMINATION

These Supplementary Product Terms shall commence upon Customer's receipt of the Configuration Modules and/or the Module Documentation, and continue in full force and effect until the termination or expiration of this Agreement.  Upon any termination or expiration of this Agreement, (a) all licenses granted to Customer under these Supplementary Product Terms shall be automatically revoked; (b) Customer shall cease all further use and distribution of the Configuration Modules; and (c) Customer shall destroy all copies of the Configuration Modules in its possession.  Without limiting Section 16 of the main body of the Agreement, Sections 1, 2.5, 3, 4, and 5 of these Supplementary Product Terms shall survive any such termination or expiration.




EOF

   # restore stdout and close output file descriptor #6
   exec 1>&6 6>&-

   # user response test
   valid_answer="false"
   
while [ "$valid_answer" = "false" ]
   do
      # send output from file descriptor 6 to stdout
      exec 6>&1
      # stdout replaced with /dev/tty. All output send to /dev/tty.
      exec>/dev/tty

      printf "Do you accept the Moogsoft license terms [yes/no] "
      # restore stdout and close output file descriptor #6
      exec 1>&6 6>&-

      # redirect stdin to fd6
      exec 6<&0
      # open /dev/tty as stdin
      exec 0</dev/tty
      read reply leftover

      if [ "$leftover" != "" ]
      then
         # send output from file descriptor 6 to stdout
         exec 6>&1
         # stdout replaced with /dev/tty. All output send to /dev/tty.
         exec>/dev/tty
         echo "Please enter only one answer"
         echo ""
         # restore stdout and close output file descriptor #6
         exec 1>&6 6>&-
         valid_answer="false"
      else
         f_reply=`echo $reply | tr '[:upper:]' '[:lower:]'`
         case $reply in
            yes) valid_answer="true"
		echo "Licence accepted, thank you";;
            no)
               # send output from file descriptor 6 to stdout
               exec 6>&1
               # stdout replaced with /dev/tty. All output send to /dev/tty.
               exec>/dev/tty
               echo ""
               echo "Your install is being canceled; to install Incident.MOOG you must accept the Moogsoft license terms by installing the moogsoft-eula package and accepting the license."
               # restore stdout and close output file descriptor #6
               exec 1>&6 6>&-
               valid_answer="true"
               exit 1;;
            *)
               # send output from file descriptor 6 to stdout
               exec 6>&1
               # stdout replaced with /dev/tty. All output send to /dev/tty.
               exec>/dev/tty
               echo "$reply is not a valid answer"
               # restore stdout and close output file descriptor #6
               exec 1>&6 6>&-
               valid_answer="false";;
         esac
      fi
   done

   # restore stdin and close input fd6
   exec 0<&6 6<&-

fi

getent group moogsoft > /dev/null || groupadd -r moogsoft
getent passwd moogsoft > /dev/null || useradd -r -g moogsoft moogsoft

%build

%install

%{__mkdir_p} %{buildroot}/%{_datadir}/moogsoft/etc/eula
%{__cp} -Rip ./eula %{buildroot}/%{_datadir}/moogsoft/etc

%clean

rm -rf %{buildroot}

%post

chown -R moogsoft:moogsoft %{_datadir}/moogsoft

%preun

##which files to include in the whole process
%files
%defattr(-,moogsoft,moogsoft,-)
%dir %{_datadir}/moogsoft/etc/eula
%{_datadir}/moogsoft/etc/eula/accepted-incident-eula.txt

%postun

%changelog
