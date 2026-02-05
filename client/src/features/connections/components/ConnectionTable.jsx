import {useState} from 'react'
import styles from '../../../shared/components/Table.module.css'
import HubSpotLogo from '../assets/Hubspot-Logo.jpg';
import Five9Logo from '../assets/Five9-Logo.jpg';
import PropTypes from 'prop-types'


function ConnectionTable({data, onSelectionChange}) {
      const headers = ["Type", "Name", "Created", "Status"]; 
      let i = 0;
      const [selectedIds, setSelectedIds] = useState([]);
    
      // Select/deselect all checkboxes
      const handleSelectAll = (event) => {
        let updated;
        if (event.target.checked) {
          // Select all IDs
          updated = data.map(record => record.id);
        } else {
          // Deselect all
          updated = [];
        }
        setSelectedIds(updated);
        if (onSelectionChange) onSelectionChange(updated);
      };
    
      // Add/remove selected connection IDs to/from a list
      const handleCheckboxChange = (id) => (event) => {
        let updated;
        (event.target.checked) ? updated = [...selectedIds, id] : updated = selectedIds.filter((item) => item !== id);
        setSelectedIds(updated);
        if (onSelectionChange) onSelectionChange(updated); // 🔁 Notify parent
      };

       // Handle row click to toggle selection
  const handleRowClick = (id) => (event) => {
    // Prevent row click if clicking on an input directly
    if (event.target.tagName === 'INPUT') return;
    
    // Prevent row click if clicking on or inside a toggle cell
    const toggleCell = event.target.closest('[data-toggle-cell]');
    if (toggleCell) return;
    
      // Toggle checkbox
      let updated;
      if (selectedIds.includes(id)) {
        updated = selectedIds.filter((item) => item !== id);
      } else {
        updated = [...selectedIds, id];
      }
      setSelectedIds(updated);
      if (onSelectionChange) onSelectionChange(updated);

  };


// Check if all items are selected
  const allSelected = data.length > 0 && selectedIds.length === data.length;
  
  return (
    <table className={styles.table}> 
      <thead>
            <th key={i++} className={styles.table__header}>
                <input 
                type="checkbox" 
                onChange={handleSelectAll}
                checked={allSelected}
                />
            </th>

            {headers.map((header) => (
                <th key={i++} className={styles.table__header}>{header}</th>
            ))}  

            <th key={i++} className={styles.table__header}>Description</th>
      </thead>
      
      <tbody>
            {data.map((record, rowIndex) => (
                <tr 
                key={`row-${rowIndex}`} 
                className={`${styles.table__row}`}
                onClick={handleRowClick(record.id)}
                >
                                
                <td key={`checkbox-${rowIndex}`} className={styles.table__data}>
                <input type="checkbox" onChange={handleCheckboxChange(record.id)} 
                    checked={selectedIds.includes(record.id)}/>
                </td>
                                            
                {record.cells && record.cells.map((cell, cellIndex) => {
                    // Check if this is the first cell (Type column) and show logo
                    const isTypeColumn = cellIndex === 0;
                    const showLogo = isTypeColumn && (cell === 'CRM' || cell === 'VCC');
                    
                    return (
                    <td key={`cell-${rowIndex}-${cellIndex}`} className={styles.table__data}>
                        {showLogo ? (
                        <img 
                            src={cell === 'CRM' ? HubSpotLogo : Five9Logo} 
                            alt={cell} 
                            style={{width: '80px', height: 'auto', borderRadius: '5px'}}
                        />
                        ) : (
                        cell
                        )}
                    </td>
                    );
                })}
            </tr>
         ))}
          
      </tbody>

    </table>
  )
}

ConnectionTable.propTypes = {
  data: PropTypes.array.isRequired,
  onSelectionChange: PropTypes.func.isRequired
}

export default ConnectionTable
