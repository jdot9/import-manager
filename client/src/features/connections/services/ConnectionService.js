const API_BASE_URL = 'http://localhost:8080/api';

class ConnectionService {

async saveConnection(connection, setErrorMessage) {
  try {
    console.log('Saving connection:', JSON.stringify(connection, null, 2));
    const response = await fetch(`${API_BASE_URL}/connections`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(connection)
    });

    if (response.ok) {
      return { success: true, message: 'Connection saved successfully' };
    } else {
      const errorText = await response.text();
      //alert('Error: ' + errorText);
      setErrorMessage(errorText)
      return false;
    }
  } catch (error) {
    console.error('Error saving connection:', error);
    setErrorMessage('Error saving connection: ' + error.message)
    return false;
  }
}

async getFormats() {
    try {
        const response = await fetch(`${API_BASE_URL}/mapping-formats`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })

        if (response.ok) {
            const data = await response.json();
            return data;
        } else {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to get formats');
        }

    } catch (error) {
        console.error('Error getting formats:', error);
        throw error;
    }
}

async saveMapping(selectedProperty, selectedFive9Field) {
    try {
        const response = await fetch(`${API_BASE_URL}/mapping-formats/save`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                hubspotProperty: selectedProperty,
                five9ContactField: selectedFive9Field
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to save mapping');
        }

        return true;
    } catch (error) {
        console.error('Error saving mapping:', error);
        throw error;
    }
}


    async getConnectionsForUserByUuid(userUuid) {
        try {
            const response = await fetch(`${API_BASE_URL}/connections?userId=${userUuid}`,{
                method: 'GET',
                headers: {
                    'Content-Type':'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch connections for user');
            }
            const data = await response.json();
            console.log('Connections retrieved for user:', data);
            return data;
        } catch (error) {
            console.error('Error fetching connections for user:', error);
            throw error;
        }
    }

    async getHubSpotProperties(connectionId) {
        try {
            const response = await fetch(`${API_BASE_URL}/hubspot-properties/${connectionId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch HubSpot properties');
            }
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Error fetching HubSpot properties:', error);
            throw error;
        }
    }

    async getFive9ContactFields(connectionId) {
        try {
            const response = await fetch(`${API_BASE_URL}/five9-contact-fields/${connectionId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch Five9 contact fields');
            }
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('Error fetching Five9 contact fields:', error);
            throw error;
        }
    }

    async deleteConnections(connectionIds) {
        console.log('Deleting connections:', connectionIds);
        const results = [];

        for (const id of connectionIds) {
            try {
                const response = await fetch(`${API_BASE_URL}/connections/${id}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                if (response.ok) {
                    results.push({ id, success: true });
                } else {
                    const errorText = await response.text();
                    results.push({ id, success: false, error: errorText });
                }
            } catch (error) {
                console.error(`Error deleting connection ${id}:`, error);
                results.push({ id, success: false, error: error.message });
            }
        }

        const successCount = results.filter(r => r.success).length;
        const failCount = results.filter(r => !r.success).length;

        return { 
            success: failCount === 0, 
            results,
            successCount,
            failCount
        };
    }
}

export default new ConnectionService();