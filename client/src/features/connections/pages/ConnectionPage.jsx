import { useState } from 'react'
import Table from '../../../shared/components/Table';
import Button from '../../../shared/components/Button'
import Toolbar from '../../../shared/components/Toolbar';
import ConnectionTypesModal from '../modals/ConnectionTypesModal';
import ConnectionService from '../services/ConnectionService';
import useGetConnections from '../hooks/useGetConnections'
import ConnectionTable from '../components/ConnectionTable';
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogActions from "@mui/material/DialogActions";

function ConnectionPage() {

const headers = ["Type", "Name", "Created", "Status"]; 
const [connectionModalIsOpen, setConnectionModalIsOpen] = useState(false);
const [selectedFromChild, setSelectedFromChild] = useState([]);
const [deleteError, setDeleteError] = useState(null);
const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
const { transformedData, loading, error, refetch } = useGetConnections();

// Open delete confirmation dialog
const handleDeleteClick = () => {
  setDeleteDialogOpen(true);
};

// Function to delete selected connections
const handleConfirmDelete = async () => {
  setDeleteDialogOpen(false);
  const result = await ConnectionService.deleteConnections(selectedFromChild);
  if (result.successCount > 0) {
    refetch();
    setSelectedFromChild([]);
  } else {
    setDeleteError("Failed to delete connection(s).");
  }
};

  return (
      <>
          {/* Toolbar for Creating and Deleting Connections */}
          <Toolbar title={(loading) ? "Loading Connections..." : `Connections: ${transformedData.length}`}>
              <Button style={{float: 'right'}} 
                      onClick={handleDeleteClick}
                      disabled={selectedFromChild.length === 0}> - Delete Connection </Button>

              <Button style={{float: 'right'}} 
                      onClick={{}}
                      disabled={selectedFromChild.length ===0}> + Test Connection </Button>

              <Button style={{float: 'right'}} 
                      onClick={() => setConnectionModalIsOpen(true)}> + New Connection </Button>
          </Toolbar>

                    {/* Delete Confirmation Dialog */}
          <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
            <DialogTitle>Are you sure you want to delete the selected connection(s)?</DialogTitle>
            <DialogActions>
              <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
              <Button onClick={handleConfirmDelete}>Confirm</Button>
            </DialogActions>
          </Dialog>

          {/* Error deleting connections feedback */}
          {deleteError && (
            <div style={{ color: 'red', fontWeight: '900', textAlign: 'center', backgroundColor: '#2d3e50' }}>
              {deleteError} 
            </div>
          )}

          {/* Error getting connections feedback */}
          {error && (
            <div style={{ color: 'red', fontWeight: '900', textAlign: 'center', backgroundColor: '#2d3e50' }}>
              Error loading connections: {error.message} 
            </div>
          )}

          {/* Table displays Connections retrieved from database */}
          <ConnectionTable 
            data={transformedData}
            onSelectionChange={(selectedIds) => setSelectedFromChild(selectedIds)} 
          />
          
           {/* Modal displays Connection Types: [Hubspot, Five9, Salesforce] */}
          {connectionModalIsOpen && <ConnectionTypesModal modalIsOpen={connectionModalIsOpen} 
                                                          setModalIsOpen={setConnectionModalIsOpen} 
                                                          onConnectionSaved={refetch} />}


      </>
  )
}

export default ConnectionPage
