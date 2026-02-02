import { useState, useEffect } from 'react'
import Button from '../../../shared/components/Button';
import TableNavbar from '../../../shared/components/TableNavbar';
import Typeahead from '../components/Typeahead';
import ConnectionService from '../../connections/services/ConnectionService';
import styles from '../components/Form.module.css';
import styles2 from '../../auth/pages/LoginPage.module.css'

// Step 5: Map HubSpot Properties to Five9 Contact Fields.
function NewImportModal_Step5({
  modalIsOpen, 
  setModalIsOpen, 
  hubspotConnectionId, 
  hubspotListId, 
  five9ConnectionId, 
  five9DialingList, 
  mapping,
  setMapping,
  nextMappingId,
  setNextMappingId,
  onBack, 
  onComplete
}) {

  const [hubspotProperties, setHubspotProperties] = useState([]);
  const [propertiesLoading, setPropertiesLoading] = useState(true);
  const [selectedProperty, setSelectedProperty] = useState('');

  const [five9ContactFields, setFive9ContactFields] = useState([]);
  const [five9FieldsLoading, setFive9FieldsLoading] = useState(true);
  const [selectedFive9Field, setSelectedFive9Field] = useState('');

  const [formats, setFormats] = useState([]);
  const [selectedMappingIds, setSelectedMappingIds] = useState(new Set());
  const [hoveredId, setHoveredId] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');

  const toggleMappingSelection = (id) => {
    setSelectedMappingIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(id)) {
        newSet.delete(id);
      } else {
        newSet.add(id);
      }
      return newSet;
    });
  };

  const toggleSelectAll = () => {
    if (selectedMappingIds.size === mapping.length) {
      setSelectedMappingIds(new Set());
    } else {
      setSelectedMappingIds(new Set(mapping.map((item) => item.id)));
    }
  };

  const removeSelectedMappings = () => {
    setMapping((prev) => prev.filter((item) => !selectedMappingIds.has(item.id)));
    setSelectedMappingIds(new Set());
  };


  const autoMapping = () => {
    // Create 5 mappings by pairing HubSpot properties with Five9 fields
    const newMappings = [];
    const properties = ["firstname", "lastname", "email", "phone", "zip"];
    const contactFields = ["first_name", "last_name", "email", "number1", "zip"];
    let currentId = nextMappingId;
    

    for (let i = 0; i < 5; i++) {
      const hubspotProperty = properties[i];
      const five9Field = contactFields[i];

      // Check for duplicate mapping
      const isDuplicate = mapping.some(
        (item) => item.hubspotProperty === hubspotProperty && item.five9Field === five9Field
      );

      if (!isDuplicate) {
        newMappings.push({
          id: currentId,
          hubspotConnectionId: hubspotConnectionId,
          hubspotListId: hubspotListId,
          five9ConnectionId: five9ConnectionId,
          five9DialingList: five9DialingList,
          hubspotProperty: hubspotProperty,
          five9Field: five9Field,
          five9Key: 0,
          formatId: formats.length > 0 ? formats[0].id : null
        });
        currentId++;
      }
    }

    if (newMappings.length === 0) {
      setErrorMessage('Default mappings already exist.');
      return;
    }

    setErrorMessage('');
    setMapping((prev) => [...prev, ...newMappings]);
    setNextMappingId(currentId);
  }

  const addMapping = () => {
    // Validate that selected values exist in the respective arrays
    const isValidProperty = hubspotProperties.includes(selectedProperty);
    const isValidField = five9ContactFields.includes(selectedFive9Field);

    if (!isValidProperty || !isValidField) {
      setErrorMessage('Please select a valid property or contact field.');
      return;
    }

    // Check for duplicate mapping
    const isDuplicate = mapping.some(
      (item) => item.hubspotProperty === selectedProperty && item.five9Field === selectedFive9Field
    );

    if (isDuplicate) {
      setErrorMessage('This mapping already exists.');
      return;
    }

    setErrorMessage('');
    setMapping((prev) => {
      const newMapping = [...prev, {
        id: nextMappingId,
        hubspotConnectionId: hubspotConnectionId, 
        hubspotListId: hubspotListId, 
        five9ConnectionId: five9ConnectionId, 
        five9DialingList: five9DialingList,
        hubspotProperty: selectedProperty, 
        five9Field: selectedFive9Field,
        five9Key: 0,
        formatId: formats.length > 0 ? formats[0].id : null
      }];
      console.log(newMapping);
      return newMapping;
    });
    
    setNextMappingId((prev) => prev + 1);
  }

  useEffect(() => {
    if (hubspotConnectionId) {
      setPropertiesLoading(true);
      ConnectionService.getHubSpotProperties(hubspotConnectionId)
        .then(properties => {
          setHubspotProperties(properties);
          setPropertiesLoading(false);
        })
        .catch(error => {
          console.error('Failed to load HubSpot properties:', error);
          setPropertiesLoading(false);
        });
    }
  }, [hubspotConnectionId]);

  useEffect(() => {
    if (five9ConnectionId) {
      setFive9FieldsLoading(true);
      ConnectionService.getFive9ContactFields(five9ConnectionId)
        .then(fields => {
          setFive9ContactFields(fields);
          setFive9FieldsLoading(false);
        })
        .catch(error => {
          console.error('Failed to load Five9 contact fields:', error);
          setFive9FieldsLoading(false);
        });
    }
  }, [five9ConnectionId]);

  useEffect(() => {
    ConnectionService.getFormats()
            .then(formats => {
              setFormats(formats);
            })
            .catch(error => {
              console.error('Failed to load formats:', error);
            });
  }, []);

  return (
    <div style={{backgroundColor: '#2d3e50'}}>

      <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
          <h1 style={{color: 'white', marginTop: 0, marginBottom: '1%'}}>{"Map HubSpot Properties to Five9 Contact Fields"}</h1>
          <h1 style={{color: 'white', marginTop: 0, marginBottom: '1%', cursor: 'pointer'}} onClick={() => setModalIsOpen(false)}>X</h1>
      </div>

        <div style={{padding: 0, width: '33%', margin: 'auto'}} >
          <p style={{textAlign: 'center', color: 'white', fontWeight: '500'}}>You must map at least 1 <span style={{fontStyle: 'italic'}}>phone</span> property to <span style={{fontStyle: 'italic'}}>number1</span>  Contact Field.</p>
        </div>

      {errorMessage && (
        <div style={{padding: 0, width: '33%', margin: 'auto'}} className={styles2.errorMessage}>
          <p style={{textAlign: 'center'}}>{errorMessage}</p>
        </div>
      )}
            
      <div className="table-container">
        <table style={{marginTop: "1%", backgroundColor: 'white'}}>
          <thead>
              <tr>
                  <th style={{position: 'sticky', top: 0, borderTop: 'none'}}>
                    <input 
                      type="checkbox" 
                      checked={mapping.length > 0 && selectedMappingIds.size === mapping.length}
                      onChange={toggleSelectAll}
                    />
                  </th>
                  <th style={{position: 'sticky', top: 0, borderTop: 'none'}}>HubSpot Property Name</th>
                  <th style={{position: 'sticky', top: 0, borderTop: 'none'}}>Five9 Contact Field Name</th>
          
              </tr>
          </thead>
          <tbody>

        { mapping.map((item) => (
        <tr 
          key={item.id}
          onClick={() => toggleMappingSelection(item.id)}
          onMouseEnter={() => setHoveredId(item.id)}
          onMouseLeave={() => setHoveredId(null)}
          style={{
            cursor: 'pointer',
            backgroundColor: selectedMappingIds.has(item.id) 
              ? '#e3f2fd' 
              : hoveredId === item.id 
                ? '#e0e0e0' 
                : 'transparent',
            transition: 'background-color 0.3s ease'
          }}
        >
          <td>
              <input 
                type="checkbox" 
                checked={selectedMappingIds.has(item.id)}
                onChange={() => toggleMappingSelection(item.id)}
                onClick={(e) => e.stopPropagation()}
              />
          </td>

          <td>
            {item.hubspotProperty}
          </td>

          <td>
            {item.five9Field}
          </td>
          
        </tr>
      ))}

          </tbody>
        </table>
      </div>

      <TableNavbar style={{display: 'flex', justifyContent: 'space-between', gap: '16px'}}>
        
            <Typeahead
              items={hubspotProperties}
              value={selectedProperty}
              onChange={setSelectedProperty}
              onSelect={setSelectedProperty}
              placeholder="Search HubSpot properties..."
              loading={propertiesLoading}
              style={{width: '29%'}}
            />

            <Typeahead
              items={five9ContactFields}
              value={selectedFive9Field}
              onChange={setSelectedFive9Field}
              onSelect={setSelectedFive9Field}
              placeholder="Search Five9 fields..."
              loading={five9FieldsLoading}
              style={{width: '28%', marginLeft: '3%'}}
            />

            <div style={{marginLeft: 'auto', display: 'flex', gap: '8px'}}>
            
              <Button onClick={autoMapping}>Auto Mapping</Button>
              <Button onClick={addMapping}>Create Mapping</Button>
              
              <Button onClick={removeSelectedMappings}>Remove Mapping</Button>
              <Button onClick={onBack}>Back</Button>
              <Button onClick={onComplete} disabled={mapping.length < 1 || !mapping.some(map => map.hubspotProperty === 'phone' && map.five9Field === 'number1')}>Next</Button>
            </div>
      </TableNavbar>



    </div>
  );
}

export default NewImportModal_Step5

