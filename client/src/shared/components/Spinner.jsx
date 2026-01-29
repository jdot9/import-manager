
import styles from './Spinner.module.css'
import PropTypes from 'prop-types'

function Spinner({text}) {
  return (
    <div className={styles['progressIndicator']}>
    <div className={styles['spinner']}></div>
    <span className={styles['progressText']}>
      {text}
    </span>
  </div>
  )
}

Spinner.propTypes = {
  text: PropTypes.string.isRequired
}

export default Spinner
