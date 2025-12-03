// src/pages/DashboardPage.jsx
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useWebSocket } from '../context/WebSocketContext';
import api from '../services/api';

import EntryModal from '../components/EntryModal';
import ExitModal from '../components/ExitModal';
import ChangePasswordModal from '../components/ChangePasswordModal';
import MoveItemModal from '../components/MoveItemModal';

const formatDuration = (seconds) => {
    if (isNaN(seconds) || seconds < 0) return '00:00:00';
    const h = Math.floor(seconds / 3600).toString().padStart(2, '0');
    const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
    const s = Math.floor(seconds % 60).toString().padStart(2, '0');
    return `${h}:${m}:${s}`;
};

function DashboardPage() {
    const { user, logout, hasPermission, isGuest } = useAuth();
    
    const {
        wsQueue,
        wsBacklog, 
        wsBufferCounts,
        isConnected,
        wsBacklogValue,
        wsBufferValues,
        wsBufferAvgTimes
    } = useWebSocket();
    
    const navigate = useNavigate();

    const [initialDataLoaded, setInitialDataLoaded] = useState(false);
    const [isEntryModalOpen, setEntryModalOpen] = useState(false);
    const [isExitModalOpen, setExitModalOpen] = useState(false);
    const [isChangePasswordModalOpen, setChangePasswordModalOpen] = useState(false);
    const [isMoveModalOpen, setMoveModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [timeOffset, setTimeOffset] = useState(0);
    const [syncedTime, setSyncedTime] = useState(new Date().getTime());
    const [filterBuffer, setFilterBuffer] = useState('ALL');

    // --- ESTADOS DE FALLBACK (API REST) ---
    const [fallbackQueue, setFallbackQueue] = useState([]);
    const [fallbackBacklog, setFallbackBacklog] = useState(0);
    const [fallbackBacklogValue, setFallbackBacklogValue] = useState(0);
    const [fallbackCounts, setFallbackCounts] = useState({ RTS: 0, EHA: 0, SAL: 0 });
    const [fallbackValues, setFallbackValues] = useState({ RTS: 0, EHA: 0, SAL: 0 });
    const [fallbackAvgTimes, setFallbackAvgTimes] = useState({ RTS: 0.0, EHA: 0.0 });

    // --- FUNÇÃO DE BUSCA DE DADOS (ATUALIZADA) ---
    const fetchDataApi = useCallback(async () => {
        // REMOVIDA A TRAVA: if (initialDataLoaded) return; 
        // Agora permite recarregar os dados sempre que chamada.
        
        try {
            console.log("Atualizando dados do dashboard...");
            
            // Busca paralela: Lista de Pacotes + Estatísticas
            const [queueRes, statsRes] = await Promise.all([
                api.get('/api/packages'),
                api.get('/api/dashboard/stats')
            ]);

            setFallbackQueue(queueRes.data || []);
            
            // Atualiza estatísticas com os dados reais do Java
            const stats = statsRes.data;
            setFallbackBacklog(stats.backlogCount || 0);
            setFallbackBacklogValue(stats.backlogValue || 0);
            setFallbackCounts(stats.counts || { RTS: 0, EHA: 0, SAL: 0 });
            setFallbackValues(stats.values || { RTS: 0, EHA: 0, SAL: 0 });
            setFallbackAvgTimes(stats.avgTimes || { RTS: 0.0, EHA: 0.0 });

        } catch (error) {
            console.error("Falha ao buscar dados via API", error);
            // Em caso de erro, mantém ou zera os dados (opcional)
        } finally {
            setInitialDataLoaded(true);
        }
    }, []); // Dependências limpas para evitar loops

    // Sincronização de Tempo
    useEffect(() => {
        const syncTime = async () => {
             try {
                const response = await api.get('/public/time');
                const serverTime = new Date(response.data.serverTime).getTime();
                const localTime = new Date().getTime();
                setTimeOffset(serverTime - localTime);
            } catch (error) {
                console.error("Falha ao sincronizar o tempo:", error);
                setTimeOffset(0);
            }
        };
        syncTime();
    }, []);

    // Carregamento Inicial
    useEffect(() => {
        if (!initialDataLoaded) {
             fetchDataApi();
        }
    }, [initialDataLoaded, fetchDataApi]); 

    // Relógio em Tempo Real
    useEffect(() => {
        const interval = setInterval(() => {
            setSyncedTime(new Date().getTime() + timeOffset);
        }, 1000);
        return () => clearInterval(interval);
    }, [timeOffset]);

    // --- SELEÇÃO DE DADOS (Fallback vs WebSocket) ---
    const useFallbackData = isGuest || !isConnected;
    const currentQueue = useFallbackData ? fallbackQueue : wsQueue;
    const currentBacklog = useFallbackData ? fallbackBacklog : wsBacklog;
    const currentBacklogValue = useFallbackData ? fallbackBacklogValue : wsBacklogValue;
    const currentCounts = useFallbackData ? fallbackCounts : wsBufferCounts;
    const currentValues = useFallbackData ? fallbackValues : wsBufferValues;
    const currentAvgTimes = useFallbackData ? fallbackAvgTimes : wsBufferAvgTimes;

    // Cálculos de Tempo (Maior Permanência)
    const oldestDurations = useMemo(() => {
        const now = syncedTime;
        let rtsSeconds = 0;
        let ehaSeconds = 0;
        const oldestRTS = currentQueue.find(item => item.buffer === 'RTS');
        const oldestEHA = currentQueue.find(item => item.buffer === 'EHA');
        
        if (oldestRTS) {
            rtsSeconds = Math.max(0, Math.floor((now - new Date(oldestRTS.entryTimestamp).getTime()) / 1000));
        }
        if (oldestEHA) {
            ehaSeconds = Math.max(0, Math.floor((now - new Date(oldestEHA.entryTimestamp).getTime()) / 1000));
        }
        return {
            rts: { item: oldestRTS, duration: rtsSeconds },
            eha: { item: oldestEHA, duration: ehaSeconds }
        };
    }, [currentQueue, syncedTime]);

    // Filtro de Buffer
    const filteredQueue = useMemo(() => {
         if (filterBuffer === 'ALL') return currentQueue;
        return currentQueue.filter(item => item.buffer === filterBuffer);
    }, [currentQueue, filterBuffer]);

    // Modais
    const openMoveModal = (item) => { setSelectedItem(item); setMoveModalOpen(true); };
    const closeMoveModal = () => { setSelectedItem(null); setMoveModalOpen(false); };

    // --- ATUALIZAÇÃO AUTOMÁTICA APÓS AÇÃO ---
    const handleSuccess = useCallback(() => {
        console.log("Ação concluída! Atualizando dados...");
        fetchDataApi(); // Chama a API novamente para atualizar a lista
    }, [fetchDataApi]);

    if (!initialDataLoaded) {
        return <p className="loading-message">A carregar dados...</p>;
    }

    return (
        <div className="app-container dashboard-container">
			{!isGuest && !isConnected && (
                <p style={{ color: 'orange', textAlign: 'center', marginBottom: '1rem', fontSize: '0.8rem' }}>
                    Modo Offline (API REST)
                </p>
            )}

            <header className="dashboard-header">
               <div><h1>FIFO</h1><p>Sistema de Controle Logístico</p></div>
               <div className="user-profile"><span>{user?.username}</span>{!isGuest && (<button onClick={() => setChangePasswordModalOpen(true)} className="change-password-button">ALTERAR SENHA</button>)}<button onClick={logout} className="logout-button">SAIR</button></div>
            </header>

            <main>
                <section className="metrics-grid">
                    <div className="metric-card">
                        <span className="metric-value">{currentBacklogValue}</span>
                        <span className="metric-label">Back-Log Total (Produtos)</span>
                        <span className="metric-label" style={{color: 'var(--color-primary)', marginTop: '4px'}}>({currentBacklog} Itens)</span>
                    </div>
                     <div className="metric-card buffer-card">
                        <div className="buffer-count"><span>RTS:</span><span>{currentCounts.RTS}</span></div>
                        <div className="buffer-count"><span>EHA:</span><span>{currentCounts.EHA}</span></div>
                        <div className="buffer-count"><span>SALVADOS:</span><span>{currentCounts.SAL}</span></div>
                    </div>
                    <div className="metric-card buffer-card">
                        <div className="buffer-count"><span>RTS:</span><span>{currentValues.RTS}</span></div>
                        <div className="buffer-count"><span>EHA:</span><span>{currentValues.EHA}</span></div>
                        <div className="buffer-count" style={{opacity: 0.5}}><span>SALVADOS:</span><span>{currentValues.SAL}</span></div>
                    </div>

                    <div className="metric-card">
                        <span className="metric-value">{formatDuration(oldestDurations.rts.duration)}</span>
                        <span className="metric-sub-label">{oldestDurations.rts.item ? oldestDurations.rts.item.trackingId : '---'}</span>
                        <span className="metric-label">Maior Tempo RTS</span>
                    </div>
                    <div className="metric-card">
                        <span className="metric-value">{formatDuration(oldestDurations.eha.duration)}</span>
                        <span className="metric-sub-label">{oldestDurations.eha.item ? oldestDurations.eha.item.trackingId : '---'}</span>
                        <span className="metric-label">Maior Tempo EHA</span>
                    </div>
                     <div className="metric-card buffer-card">
                        <div className="buffer-count"><span>RTS Médio:</span><span>{formatDuration(currentAvgTimes.RTS)}</span></div>
                        <div className="buffer-count"><span>EHA Médio:</span><span>{formatDuration(currentAvgTimes.EHA)}</span></div>
                         <div className="buffer-count" style={{visibility: 'hidden'}}><span>&nbsp;</span><span>&nbsp;</span></div>
                    </div>
                </section>

				<section className="filter-controls">
                  <label htmlFor="buffer-filter" className="filter-label">Filtrar Buffer:</label>
                  <select id="buffer-filter" className="dashboard-filter-select" value={filterBuffer} onChange={(e) => setFilterBuffer(e.target.value)}>
                    <option value="ALL">TODOS ({currentCounts.RTS + currentCounts.EHA + currentCounts.SAL})</option>
                    <option value="RTS">RTS ({currentCounts.RTS})</option>
                    <option value="EHA">EHA ({currentCounts.EHA})</option>
                    <option value="SAL">SALVADOS ({currentCounts.SAL})</option>
                  </select>
                </section>

                <section className="fifo-list">
                    <header className={`fifo-list-header ${!isGuest ? 'with-actions' : ''}`}>
                       <span>ID</span>
                       <span>PERFIL</span>
                       <span>BUFFER</span>
                       <span>RUA</span>
                       <span>DURAÇÃO</span>
                       {!isGuest && <span>AÇÕES</span>}
                    </header>
                    <div className="fifo-list-body">
                        {filteredQueue.length > 0 ? filteredQueue.map(item => {
                            const entryTimestamp = new Date(item.entryTimestamp).getTime();
                            const durationSeconds = Math.max(0, Math.floor((syncedTime - entryTimestamp) / 1000));
                            return (
                                <div className={`fifo-list-item ${!isGuest ? 'with-actions' : ''}`} key={item.id}>
                                    <span>{item.trackingId}</span>
                                    <span>{item.profileType !== 'N/A' ? item.profileType : '---'}</span>
                                    <span>{item.buffer}</span>
                                    <span>{item.rua}</span>
                                    <span>{formatDuration(durationSeconds)}</span>
                                    {!isGuest && (
                                        <div className="action-buttons-cell">
                                            {hasPermission('MOVE_PACKAGE') && (<button onClick={() => openMoveModal(item)} className="move-btn">Mover</button>)}
                                        </div>
                                    )}
                                </div>
                            );
                        }) : (
                           <p className="empty-queue-message">{filterBuffer === 'ALL' ? 'A fila está vazia.' : `Nenhum item no buffer ${filterBuffer}.`}</p>
                        )}
                    </div>
                </section>

				 {!isGuest && hasPermission('MANAGE_FIFO') && (<section className="actions-grid"><button className="action-button entry" onClick={() => setEntryModalOpen(true)}>ENTRADA</button><button className="action-button exit" onClick={() => setExitModalOpen(true)}>SAÍDA</button></section>)}
                 <div className="admin-nav-buttons">{!isGuest && hasPermission('GENERATE_QR_CODES') && (<button onClick={() => navigate('/qrcode-generator')} className="admin-nav-button">GERAR QR CODES</button>)}{!isGuest && hasPermission('VIEW_LOGS') && (<button onClick={() => navigate('/logs')} className="admin-nav-button">VER LOGS DE ATIVIDADE</button>)}{!isGuest && hasPermission('VIEW_USERS') && (<button onClick={() => navigate('/management')} className="admin-nav-button">PAINEL DE GESTÃO</button>)}</div>
            </main>

			<ChangePasswordModal isOpen={isChangePasswordModalOpen} onClose={() => setChangePasswordModalOpen(false)} />
            {/* Passando handleSuccess para todos os modais que alteram dados */}
            <EntryModal isOpen={isEntryModalOpen} onClose={() => setEntryModalOpen(false)} onSuccess={handleSuccess} />
            <ExitModal isOpen={isExitModalOpen} onClose={() => setExitModalOpen(false)} onSuccess={handleSuccess} availableIDs={currentQueue.map(item => item.trackingId)} />
            <MoveItemModal isOpen={isMoveModalOpen} onClose={closeMoveModal} onSuccess={handleSuccess} item={selectedItem} />
        </div>
    );
}

export default DashboardPage;