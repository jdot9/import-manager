import Card from '../../../shared/components/Card'
import { useState } from 'react'
import Modal from 'react-modal'
import PropTypes from 'prop-types'
import Button from '../../../shared/components/Button';
import TableNavbar from '../../../shared/components/TableNavbar'
import ConnectionList from '../components/ConnectionList'
import Connection from '../components/Connection'
import SelectedConnectionModal from './SelectedConnectionModal'
import hubspotlogo from '../assets/Hubspot-Logo.jpg'
import five9logo from '../assets/Five9-Logo.jpg'
import salesforcelogo from '../assets/Salesforce-Logo.jpg'

function ConnectionTypesModal({ modalIsOpen, setModalIsOpen, onConnectionSaved }) {

  const [step, setStep] = useState(0);
  const [selectedConnection, setSelectedConnection] = useState("");

  const user = JSON.parse(localStorage.getItem("user"));
  console.log("user.uuid", user.uuid)
  
  
  const handleConnectionChange = (connectionValue) => {
    setSelectedConnection(connectionValue);
    console.log('Selected connection:', connectionValue);
  };
 
  const handleCloseModal = () => {

    setStep(0);
    setModalIsOpen(false);
  };

  return (
    <Modal
       isOpen={modalIsOpen}
       onRequestClose={handleCloseModal}
       style={{
         overlay: {
           backgroundColor: 'rgba(0, 0, 0, 0.75)',
           zIndex: '100'
         },
         content: {
           width: '100%',
           height: '100%',
           margin: 'auto',
           border: 'none',
           backgroundColor: 'transparent'
         }
       }}
       contentLabel="Connections Modal"
    >
      
    {step === 0 && (  
      <Card closeModal={handleCloseModal}>
        <h1 style={{textAlign: 'center'}}>Select a connection type</h1>

        <ConnectionList>
          <Connection logo={hubspotlogo} name="HubSpot" value="hubspot" checked={selectedConnection === "hubspot"} onChange={handleConnectionChange} />
          <Connection logo={five9logo} name="Five9" value="five9" checked={selectedConnection === "five9"} onChange={handleConnectionChange} />
          <Connection logo={salesforcelogo} name="Salesforce" value="salesforce" checked={selectedConnection === "salesforce"} onChange={handleConnectionChange} disabled={true} />
        </ConnectionList>

        <TableNavbar> 
             <Button onClick={() => setStep(3)} disabled={!selectedConnection}>Open</Button>    
        </TableNavbar>
      </Card>
    )}

    {step === 3 && (
      <SelectedConnectionModal 
        selectedConnection={selectedConnection}
        onBack={() => setStep(0)}
        onClose={handleCloseModal}
        onConnectionSaved={onConnectionSaved}
        userUuid={user.uuid}
      />
    )}
    </Modal>
  );
}

ConnectionTypesModal.propTypes = {
  modalIsOpen: PropTypes.bool.isRequired,
  setModalIsOpen: PropTypes.func.isRequired,
  onConnectionSaved: PropTypes.func
}

export default ConnectionTypesModal;
