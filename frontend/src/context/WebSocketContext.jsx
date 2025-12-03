import React, { createContext, useState, useEffect, useContext, useMemo } from 'react';
import { useAuth } from './AuthContext';

const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
    const { token, user, isGuest } = useAuth();
    const [onlineUsers, setOnlineUsers] = useState([]);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        let ws = null;

        if (token && !isGuest) {
            const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
            const wsURL = baseURL.replace(/^http/, 'ws');
            const finalWsUrl = `${wsURL}/api/ws?token=${token}`;

            ws = new WebSocket(finalWsUrl);

            ws.onopen = () => {
                setIsConnected(true);
            };

            ws.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    if (message.type === 'online_users') {
                        if (user && (user.role === 'admin' || user.role === 'leader')) {
                            setOnlineUsers(message.data || []);
                        }
                    }
                } catch (e) {
                    // Falha silenciosa no parse para não poluir o console
                }
            };

            // Removemos os logs de onclose/onerror para manter o console limpo
            ws.onclose = () => {
                setIsConnected(false);
                setOnlineUsers([]);
            };
            
            ws.onerror = () => {
                setIsConnected(false);
            };
        } else {
            setOnlineUsers([]);
            setIsConnected(false);
        }

        return () => {
            if (ws) ws.close();
        };
    }, [token, isGuest, user]);

    const value = useMemo(() => ({
        onlineUsers,
        isConnected
    }), [onlineUsers, isConnected]);

    return (
        <WebSocketContext.Provider value={value}>
            {children}
        </WebSocketContext.Provider>
    );
};

export const useWebSocket = () => {
    return useContext(WebSocketContext);
};