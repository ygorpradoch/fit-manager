import { useState, useRef, useEffect } from 'react';
import './Chatbot.css';

const GEMINI_KEY = import.meta.env.VITE_GEMINI_KEY ?? '';
const GEMINI_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=${GEMINI_KEY}`;

const SYSTEM_PROMPT = `Você é o assistente virtual da UNIFOR GYM. Responda APENAS dúvidas sobre:
- Horários da academia (Seg-Sex: 6h às 22h, Sáb: 8h às 18h)
- Como usar o aplicativo FitManager
- Informações gerais da academia UNIFOR

Recuse educadamente qualquer pergunta sobre treinos, dietas, medicamentos ou orientações médicas, e sugira consultar o profissional adequado.`;

type Role = 'user' | 'assistant';
interface Msg { id: number; role: Role; text: string; hora: string; }

const hora = () => new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

export default function ChatBot() {
  const [aberto, setAberto] = useState(false);
  const [msgs, setMsgs] = useState<Msg[]>([
    { id: 1, role: 'assistant', text: 'Olá! Sou o assistente virtual do UNIFOR GYM. Como posso ajudar?', hora: hora() }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const fim = useRef<HTMLDivElement>(null);

  useEffect(() => { fim.current?.scrollIntoView({ behavior: 'smooth' }); }, [msgs, loading]);

  const enviar = async (texto: string) => {
    const msg = texto.trim();
    if (!msg || loading) return;

    const nova: Msg = { id: Date.now(), role: 'user', text: msg, hora: hora() };
    setMsgs(prev => [...prev, nova]);
    setInput('');
    setLoading(true);

    try {
      const contents = [...msgs, nova]
        .filter(m => !(m.role === 'assistant' && m.id === 1))
        .map(m => ({ role: m.role === 'assistant' ? 'model' : 'user', parts: [{ text: m.text }] }));

      const res = await fetch(GEMINI_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ system_instruction: { parts: [{ text: SYSTEM_PROMPT }] }, contents }),
      });

      const data = await res.json();
      const resposta = data.candidates?.[0]?.content?.parts?.[0]?.text ?? 'Não consegui processar sua mensagem.';
      setMsgs(prev => [...prev, { id: Date.now(), role: 'assistant', text: resposta, hora: hora() }]);
    } catch {
      setMsgs(prev => [...prev, { id: Date.now(), role: 'assistant', text: 'Erro ao conectar. Tente novamente.', hora: hora() }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <button className={`cb-fab ${aberto ? 'cb-fab--ativo' : ''}`} onClick={() => setAberto(v => !v)}>
        {aberto ? '✕' : '💬'}
      </button>

      {aberto && (
        <div className="cb-janela">
          <div className="cb-header">
            <div className="cb-header-info">
              <div className="cb-avatar">🤖</div>
              <div>
                <p className="cb-header-nome">Assistente Virtual</p>
                <p className="cb-header-sub">UNIFOR GYM</p>
              </div>
            </div>
            <button className="cb-fechar" onClick={() => setAberto(false)}>✕</button>
          </div>

          <div className="cb-mensagens">
            {msgs.map(m => (
              <div key={m.id} className={`cb-msg cb-msg--${m.role}`}>
                {m.role === 'assistant' && <div className="cb-msg-avatar">🤖</div>}
                <div className="cb-msg-conteudo">
                  <p className="cb-msg-texto">{m.text}</p>
                  <span className="cb-msg-hora">{m.hora}</span>
                </div>
              </div>
            ))}
            {loading && (
              <div className="cb-msg cb-msg--assistant">
                <div className="cb-msg-avatar">🤖</div>
                <div className="cb-msg-conteudo">
                  <div className="cb-digitando"><span /><span /><span /></div>
                </div>
              </div>
            )}
            <div ref={fim} />
          </div>

          <div className="cb-rapidas">
            <p className="cb-rapidas-label">Perguntas rápidas:</p>
            <div className="cb-rapidas-lista">
              {['Qual o horário da academia?', 
              'Como visualizo meu treino?', 
              'Como funciona o FitManager?'].map(p => (
                <button key={p} className="cb-rapida-btn" onClick={() => enviar(p)}>{p}</button>
              ))}
            </div>
          </div>

          <div className="cb-input-area">
            <input
              className="cb-input"
              type="text"
              placeholder="Digite sua dúvida..."
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && enviar(input)}
              disabled={loading}
            />
            <button className="cb-enviar" onClick={() => enviar(input)} disabled={!input.trim() || loading}>➤</button>
          </div>
        </div>
      )}
    </>
  );
}