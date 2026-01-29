import styles from './Card.module.css'
import PropTypes from 'prop-types'

function Card({children, style, title, closeModal, logo}) {
  return (
    <div className={`${styles['card']}`} style={style}>
        <div className={`${styles['card-header']}`}>
            {title && <h1 className={`${styles['card-header__title']}`}>{title}</h1>}
            {logo && <img src={logo} alt="Logo" className={`${styles['card-header__logo']}`} />}
            {closeModal && (
              <h1 className={`${styles['card-header__exit']}`} onClick={closeModal}>X</h1>
            )}
        </div>
        {children}
    </div>
  )
}

Card.propTypes = {
  children: PropTypes.node.isRequired,
  style: PropTypes.object,
  title: PropTypes.string,
  closeModal: PropTypes.func,
  logo: PropTypes.string
}

export default Card
