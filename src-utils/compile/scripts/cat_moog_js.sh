#!/bin/bash
#############################################################
#                                                           #
#  Contents of file Copyright (c) Moogsoft Inc 2014         #
#                                                           #
#-----------------------------------------------------------#
#                                                           #
#  WARNING:                                                 #
#  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY               #
#  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND    #
#  WHOLLY OWNED SUBSIDIARY COMPANIES.                       #
#  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:         #
#                                                           #
#  This source code is confidential and any person who      #
#  receives a copy of it, or believes that they are viewing #
#  it without permission is asked to notify Phil Tee        #
#  on 07734 591962 or email to phil@moogsoft.com.           #
#  All intellectual property rights in this source code     #
#  are owned by Moogsoft Inc.  No part of this source code  #
#  may be reproduced, adapted or transmitted in any form or #
#  by any means, electronic, mechanical, photocopying,      #
#  recording or otherwise.                                  #
#                                                           #
#  You have been warned....so be good for goodness sake...  #
#                                                           #
#############################################################

if [ -z "$MOOGSOFT_SRC_HOME" ]; then
    echo "Need to set Moogsoft Home"
    exit 1
fi

cd $MOOGSOFT_SRC_HOME/ui/js/

if [ -e  moog_all.js ]
then
    rm moog_all.js
fi

cat CLogger.js >> moog_all.js
cat common/CThemeManager.js >> moog_all.js
cat MOOG.js >> moog_all.js
cat CCreateSig.js >> moog_all.js
cat common/CTimeZone.js >> moog_all.js
cat CTimer.js >> moog_all.js
cat CParameters.js >> moog_all.js
cat CLongPoller.js >> moog_all.js
cat CFilter.js >> moog_all.js
cat CFilterBuilder.js >> moog_all.js
cat CViewManager.js >> moog_all.js
cat CModel.js >> moog_all.js
cat incident/CMediaObject.js >> moog_all.js
cat incident/CMediaManager.js >> moog_all.js
cat CMoogDesktop.js >> moog_all.js
cat CMoogDock.js >> moog_all.js
cat CDesktopBackground.js >> moog_all.js
cat CActivityMgr.js >> moog_all.js
cat CGraphicsResources.js >> moog_all.js
cat incident/alert/CAlertModel.js >> moog_all.js
cat CDataStore.js >> moog_all.js
cat CSummaryBadge.js >> moog_all.js
cat CClock.js >> moog_all.js
cat settings/hotkeys/CHotkeyModel.js >> moog_all.js
cat incident/situation/CSigTable.js >> moog_all.js
cat incident/situation/CSigQueueWindow.js >> moog_all.js
cat incident/situation/CSigQueueManager.js >> moog_all.js
cat CToolView.js >> moog_all.js
cat incident/tools/CToolRunner.js >> moog_all.js
cat CSshView.js >> moog_all.js
cat CFilterEditor.js >> moog_all.js
cat CSettingsBundle.js >> moog_all.js
cat CSettingsDialog.js >> moog_all.js
cat incident/situation/CSigModel.js >> moog_all.js
cat CSituationSelectorDialog.js >> moog_all.js
cat CTemplateMgr.js >> moog_all.js
cat CGraphicalTimeline.js >> moog_all.js
cat CSimSigs.js >> moog_all.js
cat CDetailsWindow.js >> moog_all.js
cat CToolsMenu.js >> moog_all.js
cat common/CConfigHandler.js >> moog_all.js
cat common/CConfigHandlerManager.js >> moog_all.js
cat common/CSystemSettingsManager.js >> moog_all.js
cat CFilterManager.js >> moog_all.js
cat CFilterDefinition.js >> moog_all.js
cat CFilterAdmin.js >> moog_all.js
cat CResponseMgr.js >> moog_all.js
cat CFilterBuilderDialog.js >> moog_all.js
cat CRenderers.js >> moog_all.js
cat CFilterToolbar.js >> moog_all.js
cat CTrendFilterWidget.js >> moog_all.js
cat CSearchUserDialog.js >> moog_all.js
cat CSearchQueueDialog.js >> moog_all.js
cat CSimpleFilterWidget.js >> moog_all.js
cat CGlobalSearchBox.js >> moog_all.js
cat CDetailsPanel.js >> moog_all.js
cat CMappedFilterWidget.js >> moog_all.js
cat CDateTimeFilterWidget.js >> moog_all.js
cat CMappedListFilterWidget.js >> moog_all.js
cat CMappedOrderedFilterWidget.js >> moog_all.js
cat CMapFilterWidget.js >> moog_all.js
cat settings/roles/CRoleModel.js >> moog_all.js
cat settings/users/CUserModel.js >> moog_all.js
cat common/CPreferenceManager.js >> moog_all.js
cat common/CIdentityManager.js >> moog_all.js
cat common/widget/CTwoPanelEditor.js >> moog_all.js
cat common/widget/CAutoCompleteField.js >> moog_all.js
cat common/widget/CFileDropBox.js >> moog_all.js
cat incident/CSystemDataModel.js >> moog_all.js
cat incident/situation/room/CSituationRoomManager.js >> moog_all.js
cat CMessageBox.js >> moog_all.js
cat common/CNotificationHelper.js >> moog_all.js
cat CMessageView.js >> moog_all.js
cat CTopBar.js >> moog_all.js
cat CLoginStatus.js >> moog_all.js
cat common/data/CDynamicPagedStore.js >> moog_all.js
cat incident/alert/CAlertStore.js >> moog_all.js
cat incident/situation/CSigStore.js >> moog_all.js
cat incident/situation/CChangeQueueDialog.js >> moog_all.js
cat incident/CResolveDialog.js >> moog_all.js
cat incident/situation/CTemplateDialog.js >> moog_all.js
cat common/widget/CListBuilder.js >> moog_all.js
cat common/widget/CFormListBuilder.js >> moog_all.js
cat incident/situation/conversation/CEntryComments.js >> moog_all.js
cat incident/situation/conversation/CEntry.js >> moog_all.js
cat incident/situation/CCreateProcessServiceDialog.js >> moog_all.js
cat incident/situation/CDescribeSituationDialog.js >> moog_all.js
cat incident/situation/CSitContextMenu.js >> moog_all.js
cat incident/situation/CSituationHistoryTree.js >> moog_all.js
cat incident/situation/CSituationHistoryDialog.js >> moog_all.js
cat incident/alert/CAlertContextMenu.js >> moog_all.js
cat common/widget/grid/CHeaderFilterGridPlugin.js >> moog_all.js
cat common/widget/grid/CHeaderFilterGridPluginMenu.js >> moog_all.js
cat incident/alert/CAlertList.js >> moog_all.js
cat incident/alert/CAlertListWindow.js >> moog_all.js
cat incident/alert/CAlertListManager.js >> moog_all.js
cat incident/situation/room/CSitHistory.js >> moog_all.js
cat incident/situation/room/CSitDetails.js >> moog_all.js
cat incident/situation/room/CSitAlertSummary.js >> moog_all.js
cat incident/situation/room/CSitTicker.js >> moog_all.js
cat incident/situation/room/CSitUsers.js >> moog_all.js
cat incident/tools/CToolUtils.js >> moog_all.js
cat incident/tools/chatops/CChatOpsExecutor.js >> moog_all.js
cat incident/tools/chatops/CChatOpsUtil.js >> moog_all.js
cat incident/situation/room/CSitConversationNewEntry.js >> moog_all.js
cat incident/situation/conversation/CSituationConversationPanel.js >> moog_all.js
cat incident/situation/room/CSitConversation.js >> moog_all.js
cat incident/situation/room/CSitAlertList.js >> moog_all.js
cat incident/situation/room/CSitTimeline.js >> moog_all.js
cat incident/situation/room/CSitKnowledge.js >> moog_all.js
cat incident/situation/room/CSitLinkPlugin.js >> moog_all.js
cat common/widget/CVerticalTabPanel.js >> moog_all.js
cat incident/situation/room/CAddThreadDialog.js >> moog_all.js
cat incident/situation/room/CSituationRoom.js >> moog_all.js
cat incident/CSystemMonitor.js >> moog_all.js
cat incident/page/CPortletPanel.js >> moog_all.js
cat incident/page/CPage.js >> moog_all.js
cat incident/page/CPageModel.js >> moog_all.js
cat incident/page/CPageManager.js >> moog_all.js
cat incident/page/CPageProperties.js >> moog_all.js
cat incident/page/CPagePropertiesDialog.js >> moog_all.js
cat incident/page/CPartsCatalogDialog.js >> moog_all.js
cat incident/page/CPageAdmin.js >> moog_all.js
cat incident/portlet/CDrillDownHelper.js >> moog_all.js
cat incident/portlet/CBarChartPart.js >> moog_all.js
cat incident/portlet/CBarChartView.js >> moog_all.js
cat incident/portlet/CSitOverviewPart.js >> moog_all.js
cat incident/portlet/CSitOverviewView.js >> moog_all.js
cat incident/portlet/CTablePart.js >> moog_all.js
cat incident/portlet/CTableView.js >> moog_all.js
cat incident/portlet/CNumberPart.js >> moog_all.js
cat incident/portlet/CNumberView.js >> moog_all.js
cat incident/portlet/CSystemMonitorPart.js >> moog_all.js
cat incident/portlet/CSystemMonitorView.js >> moog_all.js
cat incident/statistics/CStatisticSelectorDialog.js >> moog_all.js
cat incident/statistics/CStatisticsManager.js >> moog_all.js
cat incident/tools/CAlertServerToolProperties.js >> moog_all.js
cat incident/tools/CAlertServerToolEditor.js >> moog_all.js
cat incident/tools/CAlertServerToolModel.js >> moog_all.js
cat incident/tools/CAlertServerToolManager.js >> moog_all.js
cat incident/tools/CSituationServerToolProperties.js >> moog_all.js
cat incident/tools/CSituationServerToolEditor.js >> moog_all.js
cat incident/tools/CSituationServerToolModel.js >> moog_all.js
cat incident/tools/CSituationServerToolManager.js >> moog_all.js
cat settings/actions/CActionLibrary.js >> moog_all.js
cat settings/are/CActionStateModel.js >> moog_all.js
cat settings/are/CActionStateManager.js >> moog_all.js
cat settings/are/CActionStateProperties.js >> moog_all.js
cat settings/are/CActionStateAdmin.js >> moog_all.js
cat settings/are/CTransitionModel.js >> moog_all.js
cat settings/are/CTransitionManager.js >> moog_all.js
cat settings/are/CTransitionProperties.js >> moog_all.js
cat settings/are/CTransitionAdmin.js >> moog_all.js
cat settings/hotkeys/CHotkeyAdmin.js >> moog_all.js
cat settings/hotkeys/CHotkeyHelpWindow.js >> moog_all.js
cat settings/hotkeys/CHotkeyManager.js >> moog_all.js
cat settings/users/CUserAdmin.js >> moog_all.js
cat settings/users/CUserProperties.js >> moog_all.js
cat settings/roles/CRoleAdmin.js >> moog_all.js
cat settings/roles/CRoleProperties.js >> moog_all.js
cat settings/console/CSystemMonitorConsoleTab.js >> moog_all.js
cat settings/console/CSystemMonitorConsole.js >> moog_all.js
cat common/CPopoutViewHelper.js >> moog_all.js
cat common/CPromptDialog.js >> moog_all.js
cat incident/situation/CMergeSituationsWindow.js >> moog_all.js
cat settings/sigaliser/cookbook/CCookbookRecipeModel.js >> moog_all.js
cat settings/sigaliser/cookbook/CCookbookRecipeManager.js >> moog_all.js
cat settings/sigaliser/cookbook/CCookbookModel.js >> moog_all.js
cat settings/sigaliser/cookbook/CCookbookManager.js >> moog_all.js
cat settings/sigaliser/cookbook/CCookbookRecipeProperties.js >> moog_all.js
cat settings/sigaliser/cookbook/CCookbookRecipeEditor.js >> moog_all.js
cat settings/sigaliser/cookbook/CCookbookProperties.js >> moog_all.js
cat settings/sigaliser/cookbook/CCookbookEditor.js >> moog_all.js
cat settings/sigaliser/CFarmdModel.js >> moog_all.js
cat settings/sigaliser/CFarmdManager.js >> moog_all.js
cat settings/sigaliser/CMergeGroupModel.js >> moog_all.js
cat settings/sigaliser/CMergeGroupManager.js >> moog_all.js
cat settings/sigaliser/CAlgorithmSelector.js >> moog_all.js
cat settings/CClearDynamicData.js >> moog_all.js
cat incident/tools/CUrlToolModel.js >> moog_all.js
cat incident/tools/CUrlToolManager.js >> moog_all.js
cat incident/tools/CUrlToolProperties.js >> moog_all.js
cat incident/tools/CUrlToolEditor.js >> moog_all.js
cat incident/CAboutDialog.js >> moog_all.js
cat incident/CSupportInfoDialog.js >> moog_all.js
cat incident/timeline/CTimelineForensic.js >> moog_all.js
cat incident/timeline/CTimelineHeatmap.js >> moog_all.js
cat incident/timeline/CTimelineView.js >> moog_all.js
cat incident/timeline/CTimelineManager.js >> moog_all.js
cat incident/timeline/CTimelineSettings.js >> moog_all.js
cat incident/timeline/CTimelineSettingsWindow.js >> moog_all.js
cat incident/timeline/CTimelineTexts.js >> moog_all.js
cat incident/timeline/CTimelineGoToTimeWindow.js >> moog_all.js
cat incident/timeline/CTimelineData.js >> moog_all.js
cat incident/timeline/CTimelineIcons.js >> moog_all.js
cat settings/entitycatalog/CEntityCatalogManager.js >> moog_all.js
cat settings/entitycatalog/CUploadEntityCatalog.js >> moog_all.js
cat settings/console/CSystemMonitorProcessingMetrics.js >> moog_all.js
cat common/widget/CJsonFormGrid.js >> moog_all.js
cat incident/tools/CGenericServerToolModel.js >> moog_all.js
cat incident/tools/CGenericServerToolEditor.js >> moog_all.js
cat incident/tools/CGenericServerToolManager.js >> moog_all.js
cat incident/tools/CGenericServerToolProperties.js >> moog_all.js
cat common/CJoinedConfigStore.js >> moog_all.js
cat settings/chatops/CChatOpsShortcutModel.js >> moog_all.js
cat settings/chatops/CChatOpsAdmin.js >> moog_all.js
cat settings/chatops/CChatOpsShortcutManager.js >> moog_all.js
cat settings/chatops/CChatOpsHelpWindow.js >> moog_all.js
cat incident/CUISetupManager.js >> moog_all.js
cat settings/CUICustomization.js >> moog_all.js

