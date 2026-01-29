import { useState, useEffect, useCallback, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WEBSOCKET_URL = 'http://localhost:8080/ws';

export function useImportProgress() {
  const [progressMap, setProgressMap] = useState({});
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    // Create STOMP client
    const client = new Client({
      webSocketFactory: () => new SockJS(WEBSOCKET_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
    });

    client.onConnect = () => {
      console.log('WebSocket connected');
      setConnected(true);

      // Subscribe to import progress updates
      client.subscribe('/topic/import-progress', (message) => {
        const progress = JSON.parse(message.body);
        console.log('Received progress update:', progress);
        
        setProgressMap(prev => ({
          ...prev,
          [progress.importId]: progress
        }));
      });
    };

    client.onDisconnect = () => {
      console.log('WebSocket disconnected');
      setConnected(false);
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      console.error('Details:', frame.body);
    };

    client.activate();
    clientRef.current = client;

    // Cleanup on unmount
    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, []);

  // Get progress for a specific import
  const getProgress = useCallback((importId) => {
    return progressMap[importId] || null;
  }, [progressMap]);

  // Get progress display string (e.g., "50/100")
  const getProgressDisplay = useCallback((importId) => {
    const progress = progressMap[importId];
    if (!progress) return null;
    return `${progress.currentRecord}/${progress.totalRecords}`;
  }, [progressMap]);

  // Get status for a specific import (from WebSocket or fallback)
  const getStatus = useCallback((importId, fallbackStatus) => {
    const progress = progressMap[importId];
    if (!progress) return fallbackStatus;
    return progress.status;
  }, [progressMap]);

  // Get completion datetime for a specific import
  const getCompletionDatetime = useCallback((importId, fallbackDatetime) => {
    const progress = progressMap[importId];
    if (!progress || !progress.completionDatetime) return fallbackDatetime;
    return progress.completionDatetime;
  }, [progressMap]);

  return {
    connected,
    progressMap,
    getProgress,
    getProgressDisplay,
    getStatus,
    getCompletionDatetime
  };
}
