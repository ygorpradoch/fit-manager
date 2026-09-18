import { useEffect, useState } from 'react'
import axios from 'axios'
import "./styles.css"
import CriarAluno from './CriarAluno';
import EditarUsuario from './EditarUsuario';
import Confirmar from './Confirmar';
import { removeToken } from "../../services/authService";

const cliente = axios.create({
     baseURL: 'https://fitmanagerapi-production.up.railway.app/api/v1/',
     timeout: 10000
});

function GestorDashboard() {
     /* flags de carregamento e erro de requisição */
     const [isLoading, setIsLoading] = useState<boolean>(true);
     const [error, setError] = useState<string | null>(null);

     /* flags de open/close e tipo do modal */
     const [isOpen, setIsOpen] = useState(false)
     const [tipo, setTipo] = useState(0)

     /* flags de open/close do modal de edição de usuário e dados do usuário selecionado */
     const [isEditarUsuarioOpen, setIsEditarUsuarioOpen] = useState(false)
     const [usuario, setUsuario] = useState(null)

     /* flags de open/close do modal de exclusão e dados do usuários a ser deletado */
     const [isConfirmarOpen, setIsConfirmarOpen] = useState(false);
     const [pendingDelete, setPendingDelete] = useState<{ id: number; cargo: number } | null>(null);

     /* trata o pedido de exclusão */
     const handleDeleteClick = (id: number, cargo: number) => {
          setPendingDelete({ id, cargo });
          setIsConfirmarOpen(true);
          console.log("Deletar usuário de ID "+id+" e cargo "+cargo+"?")
     };

     /* confirma exclusão */
     const handleConfirm = () => {
          if (pendingDelete) {
               deletar(pendingDelete.id, pendingDelete.cargo);
          }
          setIsConfirmarOpen(false);
          setPendingDelete(null);
     };

     /* cancela exclusão */
     const handleCancel = () => {
          setIsConfirmarOpen(false);
          setPendingDelete(null);
     };

     /* resgate do token JWT */
     const token = localStorage.getItem('fitmanager_token');

     const [alunos, setAlunos] = useState({
          total: 0,
          page: 0,
          items: []
     })
     const [professores, setProfessores] = useState({
          total: 0,
          page: 0,
          items: []
     })

     const [pesquisaAluno, setPesquisaAluno] = useState('')
     const [pesquisaProfessor, setPesquisaProfessor] = useState('')

     const carregar = () => {
          setIsLoading(true)

          const headers = { Authorization: `Bearer ${token}` };

          Promise.all([
               cliente.get('alunos/', { headers }),
               cliente.get('instrutores/', { headers }),
          ])
               .then(([alunosRes, instrutoresRes]) => {
                    setAlunos(alunosRes.data);
                    setProfessores(instrutoresRes.data);
               })
               .catch((error) => {
                    setError(error.message);
               })
               .finally(() => {
                    setIsLoading(false);
               });
     }

     const deletar = (id, cargo) => {
          if (cargo !== 2 && cargo !== 3) {
               console.log("Cargo inválido: " + cargo);
               return;
          }

          const endpoint = cargo === 2 ? `/instrutores/${id}` : `/alunos/${id}`;
          //console.log("deletando usuário de cargo " + cargo + " e id " + id+" pelo endpoint"+endpoint);

          cliente.delete(endpoint, {
               headers: {
                    Authorization: `Bearer ${token}`,
               },
          })
          .then((response) => {
               console.log("Sucesso:", response)
               carregar()
          })
          .catch((error) => {
               console.error('Erro:', error);
          });
     };

     useEffect(() => {
          carregar()
     }, []);

     {/* useEffect(() => {
          if (alunos.items.length != 0) console.log(alunos);
     }, [alunos]) */}

     {/*useEffect(() => {
          if (professores.items.length != 0) console.log(professores);
     }, [professores])*/}

     if (isLoading) return (<h1>Carregando...</h1>)
     if (error) return (<p>{error}</p>)

     return (
          <>
               {
                    isOpen &&
                    <div className='modal-criar-aluno-overlay'>
                         <CriarAluno setIsOpen={setIsOpen} reload={carregar} tipoUsuario={tipo} />
                    </div>
               }

               {
                    isEditarUsuarioOpen &&
                    <div className='modal-criar-aluno-overlay'>
                         <EditarUsuario setIsOpen={setIsEditarUsuarioOpen} reload={carregar} usuario={usuario}/>
                    </div>
               }

               {isConfirmarOpen &&
                    <div className='modal-overlay-deletar'>
                         <Confirmar
                              isOpen={isConfirmarOpen}
                              title="Tem certeza?"
                              message="Esta ação não pode ser desfeita. Você tem certeza que deseja deletar este usuário?"
                              onConfirm={handleConfirm}
                              onCancel={handleCancel}
                         />
                    </div>
               }

               <div className='header-admin'>
                    <div>
                    <h1>UNIFOR GYM - Gerenciamento</h1>
                    <h2>Painel Administrativo</h2>
                    </div>
                    <div>
<button className='botao-logout-admin' onClick={removeToken}>
                         Sair
                    </button>
                    </div>
                    
               </div>
               

               <div className='card'>
                    <h1>{alunos.items.length}</h1>
                    <h2>Alunos Ativos</h2>
               </div>

               <div className='acao-rapida'>
                    <h1>Ações Rápidas</h1>
                    <div className='acao-rapida-botoes'>
                    <button onClick={() => {
                         setTipo(3);
                         setIsOpen(true);
                    }}>Novo Aluno</button>
                    <button onClick={() => {
                         setTipo(2);
                         setIsOpen(true);
                    }}>Novo Professor</button>
                    </div>
               </div>

               <div className='alunos-ativos'>
                    <h1>Alunos Ativos</h1>
                    <div className='alunos-ativos-content'>
                    <input
                         type="search"
                         className='barra-de-pesquisa'
                         value={pesquisaAluno}
                         onChange={(e) => setPesquisaAluno(e.target.value)}
                         placeholder="Buscar aluno por nome ou matrícula..."
                         inputMode="search"
                    />

                    <div className="table" role="table">
                         <div className="table-header" role="row">
                              <div role="columnheader">Nome</div>
                              <div role="columnheader">Matrícula</div>
                              <div role="columnheader">Ações</div>
                         </div>

                              {alunos.items.map((item) => (
                                   <div className="table-row" role="row" key={item.id}>
                                        <div role="cell">{item.nomeCompleto}</div>
                                        <div role="cell">{item.matricula}</div>
                                        <div role="cell"><button onClick={() => {
                                             setIsEditarUsuarioOpen(true);
                                             setUsuario(item)
                                        }}>Editar</button> <button onClick={() => handleDeleteClick(item.id, item.cargoId)}>Deletar</button></div>
                                   </div>
                              ))}
                    </div>
                    </div>
               </div>

               <div className='alunos-ativos'>
                    <h1>Professores Ativos</h1>
                    <div className='alunos-ativos-content'>
                    <input
                         type="search"
                         className='barra-de-pesquisa'
                         value={pesquisaProfessor}
                         onChange={(e) => setPesquisaProfessor(e.target.value)}
                         placeholder="Buscar professor por nome ou matrícula..."
                         inputMode="search"
                    />

                    <div className="table" role="table">
                         <div className="table-header" role="row">
                              <div role="columnheader">Nome</div>
                              <div role="columnheader">Matrícula</div>
                              <div role="columnheader">Ações</div>
                         </div>
                         {professores.items.map((item) => (
                              <div className="table-row" role="row" key={item.id}>
                                   <div role="cell">{item.nomeCompleto}</div>
                                   <div role="cell">{item.matricula}</div>
                                   <div role="cell"><button onClick={() => {
                                             setIsEditarUsuarioOpen(true);
                                             setUsuario(item)
                                        }}>Editar</button> <button onClick={() => handleDeleteClick(item.id, item.cargoId)}>Deletar</button></div>
                              </div>
                         ))}
                    </div>
                    </div>
               </div>
          </>
     )
}

export default GestorDashboard