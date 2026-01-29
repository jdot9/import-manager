import PropTypes from 'prop-types';
import { useState } from 'react';

function TableImport({ headers = [], data = [], useRadio = false, onSelectionChange, style, initialSelectedId = null }) {
  const [selectedId, setSelectedId] = useState(initialSelectedId);

  const handleRowClick = (id) => (event) => {
    // Prevent row click if clicking on the input directly
    if (event.target.tagName === 'INPUT') return;
    
    if (useRadio && onSelectionChange) {
      setSelectedId(id);
      onSelectionChange([id]);
    }
  };

  const handleRadioChange = (id) => {
    setSelectedId(id);
    if (onSelectionChange) {
      onSelectionChange([id]);
    }
  };

  return (
    <div className="table-container">
    <table style={{marginTop: "0%", backgroundColor: 'white', ...style}}>
        <thead>
            <tr>
                {useRadio && <th style={{position: 'sticky', top: 0, borderTop: 'none'}}>Select</th>}
                {headers.map((header, index) => (
                  <th key={`header-${index}`} style={{position: 'sticky', top: 0, borderTop: 'none'}}>{header}</th>
                ))}
            </tr>
        </thead>
        <tbody>
            {data.map((record, index) => {
              const isSelected = selectedId === record.id;
              const backgroundColor = isSelected 
                ? '#b3d9ff' 
                : index % 2 === 0 ? 'white' : 'rgb(245, 248, 250)';
              
              return (
                <tr 
                  key={record.id || index} 
                  style={{backgroundColor, cursor: 'pointer', transition: 'background-color 0.3s ease'}}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = isSelected ? '#99ccff' : '#e0e0e0'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = backgroundColor}
                  onClick={handleRowClick(record.id)}
                >
                  {useRadio && (
                    <td>
                      <input 
                        type="radio" 
                        name="selectedRecord" 
                        value={record.id}
                        checked={isSelected}
                        onChange={() => handleRadioChange(record.id)} 
                      />
                    </td>
                  )}
                  {record.cells && record.cells.map((cell, cellIndex) => (
                    <td key={`cell-${index}-${cellIndex}`}>{cell}</td>
                  ))}
                </tr>
              );
            })}
        </tbody>
    </table>
</div> 
  )
}

TableImport.propTypes = {
  headers: PropTypes.array,
  data: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    cells: PropTypes.array
  })),
  useRadio: PropTypes.bool,
  onSelectionChange: PropTypes.func,
  style: PropTypes.object,
  initialSelectedId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
}

export default TableImport

