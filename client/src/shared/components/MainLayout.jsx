import PropTypes from 'prop-types';
import Navbar from './Navbar';
import Footer from './Footer';

function MainLayout({children}) {
    return (
      <>
        <Navbar /> 
          <main> {children} </main>
        <Footer />
      </>
    )
  }
  
  export default MainLayout
  MainLayout.propTypes = {
    children: PropTypes.node.isRequired,
  };
