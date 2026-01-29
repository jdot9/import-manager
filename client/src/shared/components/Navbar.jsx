import { NavLink } from "react-router";
import { useNavigate } from "react-router";
import UserService from '../../features/auth/services/AuthService';
import styles from './Navbar.module.css'

function Navbar() {
  const navigate = useNavigate();
  
  const handleLogout = () => {
    UserService.logout();
    navigate('/login');
  };

  return (
    <nav className={styles.navbar}>
        <h1 className={styles['navbar-title']}>Import Manager Demo</h1>
        <ul className={styles['navbar-list']}>
     
            <li className={`${styles['navbar-list__item']} ${styles['navbar-list__item--right']}`}>
              <a className={styles['navbar-logout-link']} onClick={handleLogout}>
                Logout
              </a>
            </li>

            <li className={`${styles['navbar-list__item']} ${styles['navbar-list__item--right']}`}> 
              <NavLink className={({isActive}) => (isActive ? styles.selected : "")} to="/imports" end> 
                Imports 
              </NavLink> 
            </li>

            <li className={`${styles['navbar-list__item']} ${styles['navbar-list__item--right']}`}> 
              <NavLink className={({isActive}) => (isActive ? styles.selected : "")} to="/connections" end> 
                Connections 
              </NavLink> 
            </li>
        </ul>
    </nav>
  )
}

export default Navbar
