import { useState, useEffect, useMemo, useRef } from "react";
import { useLocation } from "react-router";
import PropTypes from 'prop-types';
import Button from '../../../shared/components/Button';
import TableNavbar from '../../../shared/components/TableNavbar';
import TableHat from '../components/TableHat';
import TableImport from '../components/TableImport';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';

// Step 4: Select a Five9 Dialing List.
function NewImportModal_Step4({setModalIsOpen, five9ConnectionId, selectedDialingListId, onDialingListSelect, onBack}) {
   const headers = ["Name", "List Size"];
   const [data, setData] = useState([]);
   const [loading, setLoading] = useState(true);
   const [selectedId, setSelectedId] = useState(selectedDialingListId);
   const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'error' });
   const state = useLocation()?.state;
   const selectedDialingList = useRef(null);

   const handleSnackbarClose = () => {
     setSnackbar({ ...snackbar, open: false });
   };
   
   const f9ConnectionId = five9ConnectionId || state?.five9ConnectionId;
   
   const select = (selectedIds) => {
     if (selectedIds && selectedIds.length > 0) {
       setSelectedId(selectedIds[0]);
       console.log(`Selected Five9 dialing list: ${selectedIds}`);
       selectedDialingList.current = selectedIds
       console.log(selectedDialingList.current);
     }
   };

   // Transform API data to TableImport format and sort alphabetically by name
   const transformedData = useMemo(() => {
     return data
       .map((record, index) => ({
         id: record.name || index,
         cells: [record.name, record.size]
       }))
       .sort((a, b) => (a.cells[0] || '').localeCompare(b.cells[0] || ''));
   }, [data]);
       
  // Get Dialing List from Five9 Configuration Web Services API 
  // useEffect((event) => getDialingList2(event, setData, setLoading, setError),[]);
useEffect(() => {
  const controller = new AbortController();

  const fetchData = async () => {
    setLoading(true);
    console.log("Getting Five9 Dialing Lists for connection id:", f9ConnectionId);
    try {
      const response = await fetch("http://localhost:8080/api/connections/five9/dialing-lists", {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ id: f9ConnectionId }),
        signal: controller.signal
      });

      if (!response.ok) {
        throw new Error("Network response was not ok");
      }

      const result = await response.json();
      setData(result);
      console.log("Five9 dialing lists retrieved:", result);
    } catch (error) {
      if (error.name === 'AbortError') {
        console.log('Fetch aborted');
      } else {
        console.error("Error:", error);
        setSnackbar({
          open: true,
          message: "Failed to retrieve Five9 dialing lists.",
          severity: 'error'
        });
      }
    } finally {
      setLoading(false);
    }
  };

  fetchData();

  return () => {
    controller.abort();
  };
}, [f9ConnectionId]);

  return (
    <div style={{backgroundColor: '#2d3e50'}}>
      
      <TableHat title="Select a Five9 VCC Dialing List" loading={loading} onClose={() => setModalIsOpen(false)} />

      <TableImport
        headers={headers}
        data={transformedData}
        useRadio={true}
        onSelectionChange={select}
        initialSelectedId={selectedDialingListId}
      />

      <TableNavbar>
        <Button onClick={() => onBack()}>Back</Button>
        <Button onClick={() => onDialingListSelect(selectedDialingList.current)} disabled={!selectedId}>Next</Button>
      </TableNavbar>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "top", horizontal: "center" }}
      >
        <Alert onClose={handleSnackbarClose} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
        
    </div>
  )
}

NewImportModal_Step4.propTypes = {
  modalIsOpen: PropTypes.bool,
  setModalIsOpen: PropTypes.func,
  hubspotConnectionId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  hubspotListId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  five9ConnectionId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  selectedDialingListId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  onDialingListSelect: PropTypes.func,
  onBack: PropTypes.func
}

export default NewImportModal_Step4

