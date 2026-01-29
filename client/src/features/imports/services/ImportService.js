const API_BASE_URL = 'http://localhost:8080/api';

class ImportService {

    async saveImport(userUuid, mapping, schedule) {
        // Extract fields that belong at top level
        const { importName, emailNotifications, email, ...scheduleData } = schedule;
        
        const importData = {
            userUuid: userUuid,
            importName: importName,
            emailNotifications: emailNotifications,
            email: email,
            mapping: mapping,
            schedule: scheduleData
        };
        try {
            const response = await fetch(`${API_BASE_URL}/imports`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(importData)
            })
            alert(JSON.stringify(importData))
            if(response.ok) {
                alert(response.text())
                return true;
            } else {
                alert(response.text())
                return false;
            }
            
        } catch (error) {
            console.error('Error saving import:', error);
            alert('Error saving import: ' + error.message);
            return false;
        }
    }

    async getImportsByUser(userUuid) {
        try {
            const response = await fetch(`${API_BASE_URL}/imports?userUuid=${userUuid}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Cache-Control': 'no-cache'
                },
                cache: 'no-store'
            });
            
            if (response.ok) {
                const data = await response.json();
                return data;
            } else {
                console.error('Failed to fetch imports:', response.statusText);
                return [];
            }
        } catch (error) {
            console.error('Error fetching imports:', error);
            return [];
        }
    }

    async deleteImport(importId) {
        try {
            const response = await fetch(`${API_BASE_URL}/imports/${importId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                return true;
            } else {
                console.error('Failed to delete import:', response.statusText);
                return false;
            }
        } catch (error) {
            console.error('Error deleting import:', error);
            return false;
        }
    }

    async startImport(importId) {
        try {
            const response = await fetch(`${API_BASE_URL}/imports/${importId}/start`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                console.log('Import started:', importId);
                return true;
            } else {
                console.error('Failed to start import:', response.statusText);
                return false;
            }
        } catch (error) {
            console.error('Error starting import:', error);
            return false;
        }
    }

    async stopImport(importId) {
        try {
            const response = await fetch(`${API_BASE_URL}/imports/${importId}/stop`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                console.log('Import stopped:', importId);
                return true;
            } else {
                console.error('Failed to stop import:', response.statusText);
                return false;
            }
        } catch (error) {
            console.error('Error stopping import:', error);
            return false;
        }
    }

}

export default new ImportService()