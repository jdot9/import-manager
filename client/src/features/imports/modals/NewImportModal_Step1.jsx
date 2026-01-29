import { useState, useEffect, useMemo } from 'react'
import PropTypes from 'prop-types';
import Button from '../../../shared/components/Button';
import TableNavbar from '../../../shared/components/TableNavbar';
import TableHat from '../components/TableHat';
import TableImport from '../components/TableImport';

// Step 1: Select a HubSpot Connection.
function NewImportModal_Step1({setModalIsOpen, selectedConnectionId, onConnectionSelect}) {

const [data, setData] = useState([]);
const [loading, setLoading] = useState(true);
const [selectedId, setSelectedId] = useState(selectedConnectionId);

const headers = ["Name", "Status"];

const setConnection = (selectedIds) => {
  if (selectedIds && selectedIds.length > 0) {
    setSelectedId(selectedIds[0]);
    sessionStorage.setItem("hubspotConnectionId", selectedIds[0]);
  }
}

// Transform API data to TableImport format
const transformedData = useMemo(() => {
  return data.map(record => ({
    id: record.id,
    cells: [record.name, record.status]
  }));
}, [data]);

// Get HubSpot Connections from database
useEffect(() => {
  const controller = new AbortController(); 
  const signal = controller.signal;

  const fetchData = async () => {
    setLoading(true);
    console.log("Getting HubSpot connections.")
    try {
      const user = JSON.parse(localStorage.getItem('user'));
      if (!user?.uuid) {
        throw new Error("User UUID not found");
      }

      const response = await fetch("http://localhost:8080/api/connections/hubspot", { 
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userUuid: user.uuid }),
        signal 
      });

      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      
      const data = await response.json();
      setData(data);
      console.log("HubSpot connections retrieved:", data)
    } catch (error) {
        console.log(error);
        if (error.name !== 'AbortError') {
          alert("Failed to retrieve HubSpot connections.");
        }
    } finally {
      setLoading(false);
    }
  };

  fetchData();

  return () => {
    controller.abort();
  };
}, []);

  return (
    <div style={{backgroundColor: '#2d3e50'}}>

      <TableHat title="Select a HubSpot Connection" loading={loading} onClose={() => setModalIsOpen(false)} />

      <TableImport
        headers={headers}
        data={transformedData}
        useRadio={true}
        onSelectionChange={setConnection}
        initialSelectedId={selectedConnectionId}
      />

      <TableNavbar>
        <Button onClick={() => onConnectionSelect(selectedId)} disabled={!selectedId}>Next</Button>
      </TableNavbar>

    </div>
  )
}

NewImportModal_Step1.propTypes = {
  setModalIsOpen: PropTypes.func,
  selectedConnectionId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  onConnectionSelect: PropTypes.func
}

export default NewImportModal_Step1

