import { useState, useEffect, useMemo } from 'react'

import Table from '../../../shared/components/Table'
import NewImportModal from '../modals/NewImportModal_Step0'
import Button from '../../../shared/components/Button'
import Toolbar from '../../../shared/components/Toolbar'
import ImportService from '../services/ImportService'
import { formatDate } from '../../../shared/util/FormatDate'
import { useImportProgress } from '../../../shared/hooks/useImportProgress'
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogActions from "@mui/material/DialogActions";


function ImportPage() {

  // Fetch Data Results
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedIds, setSelectedIds] = useState([]);
  const [toggleStates, setToggleStates] = useState({}); // Local toggle states for immediate UI response

  const [modalIsOpen, setModalIsOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  // WebSocket hook for real-time progress updates
  const { connected, getProgressDisplay, getStatus, getCompletionDatetime } = useImportProgress();

  const user = JSON.parse(localStorage.getItem("user"));

  // Handle selection change from Table component
  const handleSelectionChange = (ids) => {
    setSelectedIds(ids);
  };

  // Fetch imports function
  const fetchImports = async () => {
    if (user?.uuid) {
      setLoading(true);
      const imports = await ImportService.getImportsByUser(user.uuid);
      setData(imports);
      setLoading(false);
    }
  };

  // Fetch imports on component mount
  useEffect(() => {
    fetchImports();
  }, []);

  // Open delete confirmation dialog
  const handleDeleteClick = () => {
    if (selectedIds.length === 0) {
      alert('Please select at least one import to delete');
      return;
    }
    setDeleteDialogOpen(true);
  };

  // Delete selected imports
  const handleConfirmDelete = async () => {
    setDeleteDialogOpen(false);
    setLoading(true);
    let deletedCount = 0;

    for (const id of selectedIds) {
      const success = await ImportService.deleteImport(id);
      if (success) deletedCount++;
    }

    setSelectedIds([]);
    await fetchImports();
  };

  const headers = ["Name", "Datetime Created","Start Datetime", "Completion Datetime", "Progress", "Status"];

  // Transform data to Table component format with real-time updates
  const transformedData = useMemo(() => {
    return data.map(record => {
      // Get real-time values from WebSocket, fallback to database values
      const wsProgress = getProgressDisplay(record.id);
      const status = getStatus(record.id, record.status);
      const completionDatetime = getCompletionDatetime(record.id, record.completionDatetime);
      
      // Determine progress display - always show percentage
      let progressDisplay = "-";
      if (status === "COMPLETED") {
        // Completed imports show 100%
        progressDisplay = record.progress != null ? `${record.progress}%` : "100%";
      } else if (status === "FAILED") {
        // Failed imports show 0%
        progressDisplay = "0%";
      } else if (status === "STARTING") {
        // Starting imports show 0%
        progressDisplay = "0%";
      } else if (status === "ACTIVE" && wsProgress) {
        // During active import: use WebSocket batch progress
        const [current, total] = wsProgress.split('/').map(Number);
        if (total > 0) {
          progressDisplay = `${Math.round((current / total) * 100)}%`;
        }
      } else if (record.progress != null) {
        // Fallback to database progress
        progressDisplay = `${record.progress}%`;
      }
      
      // Use local toggle state if set, otherwise use status from backend
      const isActive = status === "ACTIVE" || status === "STARTING";
      const toggleChecked = toggleStates[record.id] !== undefined 
        ? toggleStates[record.id] 
        : isActive;

      return {
        id: record.id,
        cells: [
          record.name,
          formatDate(record.createdAt),
          formatDate(record.startDatetime),
          formatDate(completionDatetime),
          progressDisplay,
          status,
        ],
        toggleChecked
      };
    });
  }, [data, getProgressDisplay, getStatus, getCompletionDatetime, toggleStates]);

  // Handle toggle change for start/stop
  const handleToggleChange = async (importId, checked) => {
    console.log(`Toggle changed for import ${importId}: ${checked ? 'Start' : 'Stop'}`);
    
    // Update local state immediately for instant UI feedback
    setToggleStates(prev => ({ ...prev, [importId]: checked }));
    
    try {
      if (checked) {
        // Start the import
        await ImportService.startImport(importId);
      } else {
        // Stop the import
        await ImportService.stopImport(importId);
      }
      
      // Refresh data from backend
      await fetchImports();
    } finally {
      // Clear local state after backend updates
      setToggleStates(prev => {
        const newState = { ...prev };
        delete newState[importId];
        return newState;
      });
    }
  };

  sessionStorage.setItem("hubspotConnectionId", "");
  sessionStorage.setItem("hubspotListId", "");
  sessionStorage.setItem("five9ConnectionId", "");
  sessionStorage.setItem("five9DialingList", "");

  return (

    <>
         <Toolbar title={`Imports: ${data.length}`}>
       
             <Button style={{float: 'right'}} onClick={handleDeleteClick} disabled={selectedIds.length === 0}>
               - Delete Import 
             </Button>
             <Button style={{float: 'right'}} onClick={() => setModalIsOpen(true)}>+ New Import</Button>
         </Toolbar>

      {modalIsOpen && <NewImportModal modalIsOpen={modalIsOpen} setModalIsOpen={setModalIsOpen} onImportSaved={fetchImports} />}

      <Table 
        headers={headers} 
        data={transformedData} 
        useCheckbox={true}
        useToggle={true}
        toggleHeader="Stop/Start"
        onSelectionChange={handleSelectionChange}
        onToggleChange={handleToggleChange}
      />

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Are you sure you want to delete the selected import(s)?</DialogTitle>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleConfirmDelete}>Confirm</Button>
        </DialogActions>
      </Dialog>

    </>
  )
}

export default ImportPage
