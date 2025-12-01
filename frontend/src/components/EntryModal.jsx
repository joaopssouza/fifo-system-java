// src/components/EntryModal.jsx
import React, { useState } from 'react';
import api from '../services/api';

function EntryModal({ isOpen, onClose, onSuccess }) {
    const [trackingId, setTrackingId] = useState('');
    const [buffer, setBuffer] = useState('');
    const [rua, setRua] = useState('');
    const [profile, setProfile] = useState('');
    const [error, setError] = useState('');

    const clearForm = () => {
        setTrackingId('');
        setBuffer('');
        setRua('');
        setProfile('');
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!buffer) {
            setError('Por favor, selecione um buffer.');
            return;
        }
        
        if (buffer !== 'SAL' && !profile) {
            setError('Por favor, selecione um perfil de pacote para RTS ou EHA.');
            return;
        }

        try {
            // CORREÇÃO PARA O JAVA:
            // 1. profileType em vez de profile? Não, o DTO do Java usa "profile".
            // 2. Se for SAL, enviamos "N/A" para passar na validação @Pattern do Java, 
            //    já que o backend trata o N/A.
            const payload = {
                trackingId,
                buffer,
                rua,
                profile: buffer === 'SAL' ? 'N/A' : profile
            };
            
            // Rota ajustada
            await api.post('/api/packages/entry', payload);
            
            onSuccess();
            onClose(); 
            clearForm(); 
        } catch (err) {
            setError(err.response?.data?.error || err.response?.data || 'Falha ao adicionar item.');
        }
    };

    const handleClose = () => {
        clearForm();
        onClose();
    }

    if (!isOpen) return null;

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
             handleClose();
        }
    };

    return (
        <div className="modal-overlay" onMouseDown={handleOverlayClick}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Nova Entrada FIFO</h2>
                    <button className="modal-close-button" onClick={handleClose}>&times;</button>
                </div>
                <form onSubmit={handleSubmit}>
                    <label htmlFor="trackingId">ID do Item</label>
                    <input id="trackingId" type="text" value={trackingId} onChange={e => setTrackingId(e.target.value)} placeholder="Ex: CG02" required />

                    <label>Buffer</label>
                    <div className="buffer-options">
                        <button type="button" className={`buffer-button ${buffer === 'RTS' ? 'selected' : ''}`} onClick={() => setBuffer('RTS')}> RTS </button>
                        <button type="button" className={`buffer-button ${buffer === 'EHA' ? 'selected' : ''}`} onClick={() => setBuffer('EHA')}> EHA </button>
                        <button type="button" className={`buffer-button ${buffer === 'SAL' ? 'selected' : ''}`} onClick={() => { setBuffer('SAL'); setProfile(''); }}> SALVADOS </button>
                    </div>

                    {buffer && buffer !== 'SAL' && (
                        <>
                            <label>Perfil do Pacote (Quantidade)</label>
                            <div className="buffer-options">
                                <button type="button" className={`buffer-button ${profile === 'P' ? 'selected' : ''}`} onClick={() => setProfile('P')}> P (250) </button>
                                <button type="button" className={`buffer-button ${profile === 'M' ? 'selected' : ''}`} onClick={() => setProfile('M')}> M (80) </button>
                                <button type="button" className={`buffer-button ${profile === 'G' ? 'selected' : ''}`} onClick={() => setProfile('G')}> G (10) </button>
                            </div>
                        </>
                    )}

                    <label htmlFor="rua">Rua</label>
                    <input id="rua" type="text" value={rua} onChange={e => setRua(e.target.value)} placeholder="Ex: RTS-002" required />

                    <button type="submit" className="modal-submit-button blue">Adicionar à Fila</button>
                    {error && <p className="error-message">{error}</p>}
                </form>
            </div>
        </div>
    );
}

export default EntryModal;