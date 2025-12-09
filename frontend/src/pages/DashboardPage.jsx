// src/pages/DashboardPage.jsx
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
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
    const navigate = useNavigate();

    // Estados de Dados (Agora únicos, vindos da API)
    const [queue, setQueue] = useState([]);
    const [backlog, setBacklog] = useState(0);
    const [backlogValue, setBacklogValue] = useState(0);
    const [bufferCounts, setBufferCounts] = useState({ RTS: 0, EHA: 0, SAL: 0 });
    const [bufferValues, setBufferValues] = useState({ RTS: 0, EHA: 0, SAL: 0 });
    const [bufferAvgTimes, setBufferAvgTimes] = useState({ RTS: 0.0, EHA: 0.0 });
    
    const [loading, setLoading] = useState(true);
    
    // Estados de UI/Modais
    const [isEntryModalOpen, setEntryModalOpen] = useState(false);
    const [isExitModalOpen, setExitModalOpen] = useState(false);
    const [isChangePasswordModalOpen, setChangePasswordModalOpen] = useState(false);
    const [isMoveModalOpen, setMoveModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [timeOffset, setTimeOffset] = useState(0);
    const [syncedTime, setSyncedTime] = useState(new Date().getTime());
    const [filterBuffer, setFilterBuffer] = useState('ALL');

    // Função para carregar dados
const loadData = useCallback(async () => {
        try {
            // --- CORREÇÃO: Usar as mesmas rotas da API para todos (já liberadas no SecurityConfig) ---
            const queueEndpoint = '/api/packages'; 
            const statsEndpoint = '/api/dashboard/stats'; 

            const [queueRes, statsRes] = await Promise.all([
                api.get(queueEndpoint),
                api.get(statsEndpoint)
            ]);

            setQueue(queueRes.data || []);
            
            const stats = statsRes.data;
            setBacklog(stats.backlogCount || 0);
            setBacklogValue(stats.backlogValue || 0);
            setBufferCounts(stats.counts || { RTS: 0, EHA: 0, SAL: 0 });
            setBufferValues(stats.values || { RTS: 0, EHA: 0, SAL: 0 });
            setBufferAvgTimes(stats.avgTimes || { RTS: 0.0, EHA: 0.0 });

        } catch (error) {
            console.error("Erro ao carregar dashboard:", error);
        } finally {
            setLoading(false);
        }
    }, []);

    // Sincronizar Tempo
    useEffect(() => {
        api.get('/public/time').then(res => {
            const serverTime = new Date(res.data.serverTime).getTime();
            setTimeOffset(serverTime - new Date().getTime());
        }).catch(err => console.error("Erro sync tempo:", err));
    }, []);

    // Carregar dados iniciais
    useEffect(() => {
        loadData();
    }, [loadData]);

    // Relógio
    useEffect(() => {
        const interval = setInterval(() => {
            setSyncedTime(new Date().getTime() + timeOffset);
        }, 1000);
        return () => clearInterval(interval);
    }, [timeOffset]);

    // Cálculos
    const oldestDurations = useMemo(() => {
        const now = syncedTime;
        let rtsSeconds = 0;
        let ehaSeconds = 0;
        const oldestRTS = queue.find(item => item.buffer === 'RTS');
        const oldestEHA = queue.find(item => item.buffer === 'EHA');
        
        if (oldestRTS) rtsSeconds = Math.max(0, Math.floor((now - new Date(oldestRTS.entryTimestamp).getTime()) / 1000));
        if (oldestEHA) ehaSeconds = Math.max(0, Math.floor((now - new Date(oldestEHA.entryTimestamp).getTime()) / 1000));
        
        return {
            rts: { item: oldestRTS, duration: rtsSeconds },
            eha: { item: oldestEHA, duration: ehaSeconds }
        };
    }, [queue, syncedTime]);

    const filteredQueue = useMemo(() => {
        if (filterBuffer === 'ALL') return queue;
        return queue.filter(item => item.buffer === filterBuffer);
    }, [queue, filterBuffer]);

    // Ações
    const openMoveModal = (item) => { setSelectedItem(item); setMoveModalOpen(true); };
    const closeMoveModal = () => { setSelectedItem(null); setMoveModalOpen(false); };
    
    // Atualiza dados após ação bem sucedida
    const handleSuccess = () => {
        loadData();
    };

    if (loading) return <p className="loading-message">A carregar dados...</p>;

    return (
        <div className="app-container dashboard-container">
            <header className="dashboard-header">
               <div><h1>FIFO</h1><p>Sistema de Controle Logístico</p></div>
               <div className="user-profile">
                   <span>{user?.username || 'Convidado'}</span>
                   {!isGuest && <button onClick={() => setChangePasswordModalOpen(true)} className="change-password-button">ALTERAR SENHA</button>}
                   <button onClick={logout} className="logout-button">SAIR</button>
               </div>
            </header>

            <main>
                {/* Métricas */}
                <section className="metrics-grid">
                    <div className="metric-card">
                        <span className="metric-value">{backlogValue}</span>
                        <span className="metric-label">Back-Log Total</span>
                        <span className="metric-label" style={{color: 'var(--color-primary)', marginTop: '4px'}}>({backlog} Itens)</span>
                    </div>
                     <div className="metric-card buffer-card">
                        <div className="buffer-count"><span>RTS:</span><span>{bufferCounts.RTS}</span></div>
                        <div className="buffer-count"><span>EHA:</span><span>{bufferCounts.EHA}</span></div>
                        <div className="buffer-count"><span>SALVADOS:</span><span>{bufferCounts.SAL}</span></div>
                    </div>
                    <div className="metric-card buffer-card">
                        <div className="buffer-count"><span>RTS:</span><span>{bufferValues.RTS}</span></div>
                        <div className="buffer-count"><span>EHA:</span><span>{bufferValues.EHA}</span></div>
                        <div className="buffer-count" style={{opacity: 0.5}}><span>SALVADOS:</span><span>{bufferValues.SAL}</span></div>
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
                        <div className="buffer-count"><span>RTS Médio:</span><span>{formatDuration(bufferAvgTimes.RTS)}</span></div>
                        <div className="buffer-count"><span>EHA Médio:</span><span>{formatDuration(bufferAvgTimes.EHA)}</span></div>
                        <div className="buffer-count" style={{visibility: 'hidden'}}><span>&nbsp;</span><span>&nbsp;</span></div>
                    </div>
                </section>

                {/* Filtros */}
				<section className="filter-controls">
                  <label htmlFor="buffer-filter" className="filter-label">Filtrar Buffer:</label>
                  <select id="buffer-filter" className="dashboard-filter-select" value={filterBuffer} onChange={(e) => setFilterBuffer(e.target.value)}>
                    <option value="ALL">TODOS ({bufferCounts.RTS + bufferCounts.EHA + bufferCounts.SAL})</option>
                    <option value="RTS">RTS ({bufferCounts.RTS})</option>
                    <option value="EHA">EHA ({bufferCounts.EHA})</option>
                    <option value="SAL">SALVADOS ({bufferCounts.SAL})</option>
                  </select>
                </section>

                {/* Lista */}
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

                {/* Botões de Ação */}
				 {!isGuest && hasPermission('MANAGE_FIFO') && (<section className="actions-grid"><button className="action-button entry" onClick={() => setEntryModalOpen(true)}>ENTRADA</button><button className="action-button exit" onClick={() => setExitModalOpen(true)}>SAÍDA</button></section>)}
                 <div className="admin-nav-buttons">{!isGuest && hasPermission('GENERATE_QR_CODES') && (<button onClick={() => navigate('/qrcode-generator')} className="admin-nav-button">GERAR QR CODES</button>)}{!isGuest && hasPermission('VIEW_LOGS') && (<button onClick={() => navigate('/logs')} className="admin-nav-button">VER LOGS DE ATIVIDADE</button>)}{!isGuest && hasPermission('VIEW_USERS') && (<button onClick={() => navigate('/management')} className="admin-nav-button">PAINEL DE GESTÃO</button>)}</div>
            </main>

			<ChangePasswordModal isOpen={isChangePasswordModalOpen} onClose={() => setChangePasswordModalOpen(false)} />
            <EntryModal isOpen={isEntryModalOpen} onClose={() => setEntryModalOpen(false)} onSuccess={handleSuccess} />
            <ExitModal isOpen={isExitModalOpen} onClose={() => setExitModalOpen(false)} onSuccess={handleSuccess} availableIDs={queue.map(item => item.trackingId)} />
            <MoveItemModal isOpen={isMoveModalOpen} onClose={closeMoveModal} onSuccess={handleSuccess} item={selectedItem} />
        </div>
    );
}

export default DashboardPage;