import PropTypes from 'prop-types'
import styles from './TableNavbar.module.css'

function TableNavbar({children, style}) {
  return (
    <div className={styles['table-navbar']} style={style}>
      {children}
    </div>
  )
}

TableNavbar.propTypes = {
  children: PropTypes.node.isRequired,
  style: PropTypes.object
}

export default TableNavbar
