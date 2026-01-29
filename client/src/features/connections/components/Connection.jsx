
import styles from './Connection.module.css'
import PropTypes from 'prop-types'

function Connection({logo, name, value, checked, onChange, disabled = false}) {
  const handleClick = () => {
    if (!disabled) {
      onChange(value);
    }
  };

  return (
    <div 
      className={styles['connection']} 
      onClick={handleClick}
      style={{ opacity: disabled ? 0.5 : 1, cursor: disabled ? 'not-allowed' : 'pointer' }}
    >
       <img src={logo} className={styles['connection__logo']} />
       <p 
         className={styles['connection__name']}
         style={{ color: checked ? '#449fff' : 'inherit' }}
       >
         {name}
       </p>
       <input 
         type="radio" 
         name="connectionType"
         style={{display: 'none'}}
         value={value}
         checked={checked}
         disabled={disabled}
         onChange={() => onChange(value)}
         onClick={(e) => e.stopPropagation()}
       />
    </div>
  )
}

export default Connection
Connection.propTypes = {
  logo: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  checked: PropTypes.bool.isRequired,
  onChange: PropTypes.func.isRequired,
  disabled: PropTypes.bool
}
