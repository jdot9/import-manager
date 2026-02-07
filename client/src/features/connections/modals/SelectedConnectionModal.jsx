import { useState } from 'react'
import Card from '../../../shared/components/Card'
import TableNavbar from '../../../shared/components/TableNavbar'
import Button from '../../../shared/components/Button'
import PropTypes from 'prop-types'
import formStyles from '../../imports/components/Form.module.css'
import ConnectionService from '../services/ConnectionService'
import hubspotlogo from '../assets/Hubspot-Logo.jpg'
import five9logo from '../assets/Five9-Logo.jpg'
import salesforcelogo from '../assets/Salesforce-Logo.jpg'
import styles2 from '../../auth/pages/LoginPage.module.css'
import Snackbar from '@mui/material/Snackbar'
import Alert from '@mui/material/Alert'

function SelectedConnectionModal({onBack, onClose, onConnectionSaved, selectedConnection, userUuid}) {

  const [errorMessage, setErrorMessage] = useState('');
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    hubspotAccessToken: "",
    five9Username: "",
    five9Password: "",
    userUuid: userUuid
  });

  console.log('User UUID:', userUuid);

  const handleSnackbarClose = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleCreate = async () => {
    setErrorMessage('');
    const connectionData = {
      ...formData,
      userUuid: userUuid,  // Always use the prop value
      type: selectedConnection === "hubspot" ? "hubspot" : "five9"
    };
    console.log('Creating connection with data:', connectionData);
    const result = await ConnectionService.saveConnection(connectionData, setErrorMessage);
    
    if (result.success) {
      setSnackbar({
        open: true,
        message: result.message,
        severity: 'success'
      });
      // Refresh the connections list and close the modal after a short delay
      setTimeout(() => {
        if (onConnectionSaved) {
          onConnectionSaved();
        }
        if (onClose) {
          onClose();
        }
      }, 1500);
    }
  };

  function handleChange(e) {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  }

  const selectedConnectionLogo = selectedConnection === "hubspot" ? hubspotlogo : selectedConnection === "five9" ? five9logo : salesforcelogo;

  // Validate required fields based on connection type
  const isFormValid = () => {
    if (!formData.name.trim()) return false;
    
    if (selectedConnection === "hubspot") {
      return formData.hubspotAccessToken.trim();
    } else {
      return formData.five9Username.trim() && formData.five9Password.trim();
    }
  };

  return (
    <Card logo={selectedConnectionLogo} closeModal={onClose}>

            {errorMessage && (
              <div style={{padding: 0, width: '90%', margin: 'auto', marginBottom: '5%'}} className={styles2.errorMessage}>
                <p style={{textAlign: 'center'}}>{errorMessage}</p>
              </div>
            )}
    
    <form className={formStyles['form']} action="">
        <div className={formStyles['form-group']}>
            <label htmlFor='name' className={formStyles['form-group__label']}>Name</label>
            <input type='text' id='name' name='name' value={formData.name} onChange={handleChange} className={formStyles['form-group__input']} />
        </div>

        <div className={formStyles['form-group']}>
            <label htmlFor='description' className={formStyles['form-group__label']}>Description</label>
            <textarea id='description' name='description' value={formData.description} onChange={handleChange} className={formStyles['form-group__input']} />
        </div>

        {selectedConnection === "hubspot" ? (
          <>
            <div className={formStyles['form-group']}>
              <label htmlFor='hubspotAccessToken' className={formStyles['form-group__label']}>Access Token:</label>
              <input type='password' id='hubspotAccessToken' name='hubspotAccessToken' value={formData.hubspotAccessToken} onChange={handleChange} className={formStyles['form-group__input']} />
            </div>
          </>
        ) : selectedConnection === "five9" ? (
          <>
            <div className={formStyles['form-group']}>
              <label htmlFor='five9Username' className={formStyles['form-group__label']}>Username:</label>
              <input type='password' id='five9Username' name='five9Username' value={formData.five9Username} onChange={handleChange} className={formStyles['form-group__input']} />
            </div>
            <div className={formStyles['form-group']}>
              <label htmlFor='five9Password' className={formStyles['form-group__label']}>Password:</label>
              <input type='password' id='five9Password' name='five9Password' value={formData.five9Password} onChange={handleChange} className={formStyles['form-group__input']} />
            </div>
          </>
        ) : selectedConnection === "salesforce" ? (
          <>
            <div className={formStyles['form-group']}>
              <label htmlFor='salesforceConsumerKey' className={formStyles['form-group__label']}>Consumer Key:</label>
              <input type='text' id='salesforceConsumerKey' name='salesforceConsumerKey' value={formData.salesforceConsumerKey} onChange={handleChange} className={formStyles['form-group__input']} />
            </div>
            <div className={formStyles['form-group']}>
              <label htmlFor='salesforceConsumerSecret' className={formStyles['form-group__label']}>Consumer Secret:</label>
              <input type='password' id='salesforceConsumerSecret' name='salesforceConsumerSecret' value={formData.salesforceConsumerSecret} onChange={handleChange} className={formStyles['form-group__input']} />
            </div>
          </>
        ) : null}

    </form>

    <TableNavbar>
      <Button onClick={onBack}>Back</Button>
      <Button 
        onClick={handleCreate} 
        disabled={!isFormValid()}
      >
        Create
      </Button>
    </TableNavbar>

    <Snackbar
      open={snackbar.open}
      autoHideDuration={4000}
      onClose={handleSnackbarClose}
      anchorOrigin={{ vertical: "top", horizontal: "center" }}
    >
      <Alert onClose={handleSnackbarClose} severity={snackbar.severity} sx={{ width: '100%' }}>
        {snackbar.message}
      </Alert>
    </Snackbar>
  </Card>
  );
}

SelectedConnectionModal.propTypes = {
  onBack: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  onConnectionSaved: PropTypes.func,
  selectedConnection: PropTypes.string.isRequired,
  userUuid: PropTypes.string.isRequired
};

export default SelectedConnectionModal;
