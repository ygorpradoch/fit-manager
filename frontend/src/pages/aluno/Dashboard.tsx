import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './AlunoDashboard.css';
import ChatBot from '../aluno/Chatbot';

import barbell from './icons/barbell.svg';
import time from './icons/time.svg';
import user from './icons/user.svg';

import { removeToken } from "../../services/authService";

const API_BASE = 'https://fitmanagerapi-production.up.railway.app';

interface Sessao {
  idSessao: number;
  nomeSessao: string;
  grupoMuscular: string;
  exercicios: { idExercicio: number }[];
}

function AlunoDashboard() {
  const navigate = useNavigate();
  const [sessoes, setSessoes] = useState<Sessao[]>([]);
  const [nomeAluno, setNomeAluno] = useState('');

  useEffect(() => {
    const fetchDados = async () => {
      try {
        const alunoId = localStorage.getItem('alunoId');
        const token = localStorage.getItem('token') ?? '';

        if (!alunoId) return;

        
        const [alunosRes, treinoRes] = await Promise.all([
          axios.get(`${API_BASE}/api/v1/alunos`, { headers: { Authorization: `Bearer ${token}` } }),
          axios.get(`${API_BASE}/api/v1/alunos/${alunoId}/treino`, { headers: { Authorization: `Bearer ${token}` } }),
        ]);

        
        const aluno = alunosRes.data?.items?.find((a: { id: number }) => a.id === Number(alunoId));
        if (aluno) setNomeAluno(aluno.nomeCompleto);

        
        const treinoId = treinoRes.data?.treinoId;
        if (!treinoId) return;

        const { data } = await axios.get(
          `${API_BASE}/api/v1/treinos/${treinoId}/sessoes`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        setSessoes(data.sessoes ?? []);
      } catch {
        setSessoes([]);
      }
    };

    fetchDados();
  }, []);

  return (
    <>
      <div className='cabecalho'>
        <div>
          <h1>UNIFOR GYM</h1>
          <p>Bem-vindo, {nomeAluno || 'aluno'}</p>
        </div>
        <div className='header-aluno-perfil'>
          <img src={user} className='user' alt="Ícone do perfil" />
          <button className='botao-logout-aluno' onClick={removeToken}>
            Sair
          </button>
        </div>
      </div>

      <div className='principal'>
        <div className='acessoMeuTreino' onClick={() => navigate('/meu-treino')}>
          <img src={barbell} className='barbell' />
          <h1>Meu treino</h1>
          <p>Visualizar ficha de treinos</p>
          <div className='treinoItens'>
            {sessoes.slice(0, 3).map((s) => (
              <p className='treinoItem' key={s.idSessao}>{s.nomeSessao}</p>
            ))}
          </div>
        </div>

        <div className='acessoHorarios'>
          <img src={time} className='time' />
          <h1>Horários</h1>
          <p>Consultar horários de funcionamento</p>
          <p>Seg. - Sex.: 6h às 22h</p>
        </div>

        <div className='acessoHistorico'>
          <h1>Histórico Recente</h1>
          {sessoes.length === 0 ? (
            <p style={{ color: '#aaa', fontSize: '0.9rem' }}>Nenhuma sessão encontrada.</p>
          ) : (
            sessoes.map((s) => (
              <div className='historicoItem' key={s.idSessao}>
                <div>
                  <p>{s.nomeSessao}</p>
                  <p style={{ fontSize: '0.8rem', color: '#888' }}>{s.grupoMuscular}</p>
                </div>
                <p>{s.exercicios?.length ?? 0} exercícios</p>
              </div>
            ))
          )}
        </div>
      </div>

      <ChatBot />
    </>
  );
}

export default AlunoDashboard;
