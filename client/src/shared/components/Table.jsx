import styles from './Table.module.css'
import PropTypes from 'prop-types'
import { useState } from 'react';
import Toggle from './Toggle';
import HubSpotLogo from '../../features/connections/assets/Hubspot-Logo.jpg';
import Five9Logo from '../../features/connections/assets/Five9-Logo.jpg';

function Table({headers, data, useContainer = false, useCheckbox = false, useRadio = false, useToggle = false, toggleHeader = "Connect", onSelectionChange, onToggleChange, style}) {
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

  // Handle radio button selection (only one can be selected)
  const handleRadioChange = (id) => () => {
    setSelectedIds([id]);
    if (onSelectionChange) onSelectionChange([id]);
  };

  // Handle row click to toggle selection
  const handleRowClick = (id) => (event) => {
    // Prevent row click if clicking on an input directly
    if (event.target.tagName === 'INPUT') return;
    
    // Prevent row click if clicking on or inside a toggle cell
    const toggleCell = event.target.closest('[data-toggle-cell]');
    if (toggleCell) return;
    
    if (useCheckbox) {
      // Toggle checkbox
      let updated;
      if (selectedIds.includes(id)) {
        updated = selectedIds.filter((item) => item !== id);
      } else {
        updated = [...selectedIds, id];
      }
      setSelectedIds(updated);
      if (onSelectionChange) onSelectionChange(updated);
    } else if (useRadio) {
      // Select radio
      setSelectedIds([id]);
      if (onSelectionChange) onSelectionChange([id]);
    }
  };

  // Check if all items are selected
  const allSelected = data.length > 0 && selectedIds.length === data.length;
  
  return (
    <div className={`${useContainer && styles['table-container']}`}>
      <table className={styles.table} style={style}>
        
        <thead>
          <tr>
            {useCheckbox && 
            <th key={i++} className={styles.table__header}>
              <input 
                type="checkbox" 
                onChange={handleSelectAll}
                checked={allSelected}
              />
            </th>}
            {useRadio && <th key={i++} className={styles.table__header}>Select</th>}
            {headers.map((header) => (
              <th key={i++} className={styles.table__header}>{header}</th>
            ))}  
            {useToggle && <th key={i++} className={styles.table__header}>{toggleHeader}</th>}
          </tr>
        </thead>

        <tbody>
          {data.map((record, rowIndex) => (
            <tr 
              key={`row-${rowIndex}`} 
              className={`${styles.table__row}`}
              onClick={handleRowClick(record.id)}
            >
              {useCheckbox && (
                <td key={`checkbox-${rowIndex}`} className={styles.table__data}>
                  <input type="checkbox" onChange={handleCheckboxChange(record.id)} 
                    checked={selectedIds.includes(record.id)}/>
                </td>
              )}

              {useRadio && (
                <td key={`radio-${rowIndex}`} className={styles.table__data}>
                  <input type="radio" onChange={handleRadioChange(record.id)} 
                    checked={selectedIds.includes(record.id)}/>
                </td>
              )}

              
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

              {useToggle && (
                <td key={`toggle-${rowIndex}`} className={styles.table__data} data-toggle-cell="true">
                  <Toggle 
                    checked={record.toggleChecked || false}
                    onChange={(e) => onToggleChange && onToggleChange(record.id, e.target.checked)}
                  />
                </td>
              )}

            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

Table.propTypes = {
  headers: PropTypes.array.isRequired,
  data: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    cells: PropTypes.array,
    toggleChecked: PropTypes.bool
  })).isRequired,
  useContainer: PropTypes.bool,
  useCheckbox: PropTypes.bool,
  useRadio: PropTypes.bool,
  useToggle: PropTypes.bool,
  toggleHeader: PropTypes.string,
  onSelectionChange: PropTypes.func,
  onToggleChange: PropTypes.func,
  style: PropTypes.object
}

export default Table
