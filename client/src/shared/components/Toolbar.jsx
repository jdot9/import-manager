import PropTypes from 'prop-types'
import styles from './Toolbar.module.css'

function Toolbar({title, children}) {
  return (
    <div className={styles.toolbar}>
        <h1 className={styles.toolbar__title}>{title}</h1>
        {children}
    </div>
  )
}

Toolbar.propTypes = {
  title: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired
}

export default Toolbar
