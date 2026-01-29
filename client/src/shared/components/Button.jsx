import PropTypes from 'prop-types'
import styles from './Button.module.css'

function Button({children, onClick, style, className, disabled, type}) {
  return (
    <button 
      className={`${styles.btn} ${styles['btn--primary']} ${className || ''}`} 
      onClick={onClick} 
      style={style}
      disabled={disabled}
      type={type || 'button'}
    >
        {children}
    </button>
  )
}

Button.propTypes = {
  children: PropTypes.node.isRequired,
  onClick: PropTypes.func,
  style: PropTypes.object,
  className: PropTypes.string,
  disabled: PropTypes.bool,
  type: PropTypes.string
}

export default Button
