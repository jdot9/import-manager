import { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import styles from './Typeahead.module.css';

function Typeahead({ 
  items = [], 
  value, 
  onChange, 
  onSelect, 
  placeholder = 'Type to search...', 
  loading = false,
  style = {}
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const [inputValue, setInputValue] = useState(value || '');
  const wrapperRef = useRef(null);

  // Filter items based on input
  const filteredItems = items.filter(item =>
    item.toLowerCase().includes(inputValue.toLowerCase())
  );

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Sync with external value changes
  useEffect(() => {
    setInputValue(value || '');
  }, [value]);

  const handleInputChange = (e) => {
    const newValue = e.target.value;
    setInputValue(newValue);
    setIsOpen(true);
    setHighlightedIndex(-1);
    if (onChange) {
      onChange(newValue);
    }
  };

  const handleInputFocus = () => {
    setIsOpen(true);
  };

  const handleItemClick = (item) => {
    setInputValue(item);
    setIsOpen(false);
    if (onSelect) {
      onSelect(item);
    }
    if (onChange) {
      onChange(item);
    }
  };

  const handleKeyDown = (e) => {
    if (!isOpen) {
      if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
        setIsOpen(true);
      }
      return;
    }

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setHighlightedIndex(prev => 
          prev < filteredItems.length - 1 ? prev + 1 : prev
        );
        break;
      case 'ArrowUp':
        e.preventDefault();
        setHighlightedIndex(prev => prev > 0 ? prev - 1 : 0);
        break;
      case 'Enter':
        e.preventDefault();
        if (highlightedIndex >= 0 && highlightedIndex < filteredItems.length) {
          handleItemClick(filteredItems[highlightedIndex]);
        }
        break;
      case 'Escape':
        setIsOpen(false);
        break;
      default:
        break;
    }
  };

  return (
    <div className={styles.typeahead} ref={wrapperRef} style={style}>
      <input
        type="text"
        className={styles.typeahead__input}
        value={inputValue}
        onChange={handleInputChange}
        onFocus={handleInputFocus}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
      />
      {isOpen && (
        <div className={styles.typeahead__dropdown}>
          {loading ? (
            <div className={styles.typeahead__loading}>Loading...</div>
          ) : filteredItems.length === 0 ? (
            <div className={styles.typeahead__empty}>No matches found</div>
          ) : (
            filteredItems.slice(0, 50).map((item, index) => (
              <div
                key={item}
                className={`${styles.typeahead__item} ${
                  index === highlightedIndex ? styles['typeahead__item--highlighted'] : ''
                }`}
                onClick={() => handleItemClick(item)}
                onMouseEnter={() => setHighlightedIndex(index)}
              >
                {item}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

Typeahead.propTypes = {
  items: PropTypes.arrayOf(PropTypes.string),
  value: PropTypes.string,
  onChange: PropTypes.func,
  onSelect: PropTypes.func,
  placeholder: PropTypes.string,
  loading: PropTypes.bool,
  style: PropTypes.object
};

export default Typeahead;
