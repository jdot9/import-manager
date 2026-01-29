import { useId, useState, useEffect } from 'react'
import PropTypes from 'prop-types'
import './Toggle.css'

function Toggle({ checked = false, onChange = () => {} }) {
  const id = useId();
  const [mounted, setMounted] = useState(false);

  // Disable transition on initial mount, enable after a short delay
  useEffect(() => {
    const timer = setTimeout(() => setMounted(true), 100);
    return () => clearTimeout(timer);
  }, []);

  const handleClick = (e) => {
    e.stopPropagation(); // Prevent row click
    // Create a synthetic event with the new checked value
    const newChecked = !checked;
    onChange({ target: { checked: newChecked } });
  };

  return (
    <div className="toggle" onClick={handleClick}>
        <input 
          type="checkbox" 
          id={id} 
          className="toggle__input"
          checked={checked}
          onChange={() => {}} // Handled by div click
          readOnly
        />
        <label 
          htmlFor={id} 
          className={`toggle__button ${mounted ? '' : 'toggle__button--no-transition'}`}
          onClick={(e) => e.preventDefault()} // Prevent label from triggering input
        ></label>
    </div>
  )
}

Toggle.propTypes = {
  checked: PropTypes.bool,
  onChange: PropTypes.func
}

export default Toggle
