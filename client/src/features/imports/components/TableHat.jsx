import PropTypes from 'prop-types'
import styles from './TableHat.module.css'

function TableHat({title, loading, onClose}) {
  return (
    <div className={styles['table-hat']}>
      <h1 className={styles['table-hat__title']}>{(loading) ? "Loading..." : title}</h1>
      <h1 className={styles['table-hat__exit']} onClick={onClose}>X</h1>
    </div>
  )
}

TableHat.propTypes = {
  title: PropTypes.string.isRequired,
  loading: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired
}

export default TableHat

