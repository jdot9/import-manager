import { useState, useEffect, useMemo, useCallback } from "react";
import ConnectionService from "../services/ConnectionService";
import { formatDate } from "../../../shared/util/FormatDate";


function useGetConnections() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const user = useMemo(() => {
    try {
      return JSON.parse(localStorage.getItem('user'));
    } catch {
      return null;
    }
  }, []);

  const fetchConnections = useCallback(() => {
    if (!user?.uuid) return;

    setLoading(true);

    ConnectionService.getConnectionsForUserByUuid(user.uuid)
      .then(d => setData(d))
      .catch(err => {
        console.error("Error fetching connections:", err);
        setError(err);
      })
      .finally(() => setLoading(false));
  }, [user?.uuid]);

  useEffect(() => {
    fetchConnections();
  }, [fetchConnections]);

  // Get connection type string for the Table component
  // Table expects 'CRM' for HubSpot or 'VCC' for Five9 and handles logo rendering
  const getConnectionType = useCallback((record) => {
    if (record.hubspotAccessToken) {
      return 'CRM';
    } else if (record.five9Username) {
      return 'VCC';
    }
    return 'Unknown';
  }, []);

  const transformedData = useMemo(() => {
    return data.map(record => ({
      id: record.id,
      cells: [
        getConnectionType(record),
        record.name,
        formatDate(record.createdAt),
        record.status,
        record.description || '',
      ]
    }));
  }, [data, getConnectionType]);

  return { transformedData, loading, error, refetch: fetchConnections };
}

export default useGetConnections;
