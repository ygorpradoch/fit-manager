import { useState } from 'react';
import axios from 'axios';
import "./Modal.css"

const cliente = axios.create({
     baseURL: 'https://fitmanagerapi-production.up.railway.app/api/v1/',
     timeout: 10000,
});

const TIPO_USUARIO = {
     2: { cargo: 'professor', endpoint: '/instrutores' },
     3: { cargo: 'aluno', endpoint: '/alunos' },
};

function CriarAluno({ setIsOpen, reload, tipoUsuario }) {
     const token = localStorage.getItem('fitmanager_token');
     const [confirmarSenhaWarning, setConfirmarSenhaWarning] = useState('')
     const [msg, setMsg] = useState('')

     const [payload, setPayload] = useState({
          nomeCompleto: '',
          cpf: '',
          dataNascimento: '',
          email: '',
          telefone: '',
          matricula: '',
          idCargo: tipoUsuario,
          senha: '',
     });

     const handleChange = (e) => {
          const { name, value } = e.target;

          setPayload((prev) => ({
               ...prev,
               [name]: name === 'idCargo' ? Number(value) : value,
          }));
     };

     const confirmarSenha = (e) => {
          if (e.target.value != payload.senha) {
               setConfirmarSenhaWarning('As senhas não coincidem.')
          } else {
               setConfirmarSenhaWarning('')
          }
     }

     const submitData = () => {
          console.log(JSON.stringify(payload, null, 2));
          setMsg('')

          const config = TIPO_USUARIO[tipoUsuario];
          console.log('config de usuario', config)
          if (!config) {
               console.error(`Tipo de usuário inválido: ${tipoUsuario}`);
               return;
          }

          cliente
               .post(config.endpoint, payload, {
                    headers: { Authorization: `Bearer ${token}` },
               })
               .then((response) => {
                    console.log('Usuário cadastrado com sucesso:', response.data)
                    setIsOpen(false);
                    reload();
               })
               .catch((error) => {
                    console.error('Data:', error.response.data)
                    
                    if (error.status === 400) {
                         setMsg('Um ou mais campos não foram preenchidos corretamente.')
                    } else if (error.status === 409) {
                         setMsg(error.response.data.message)
                    } else {
                         setMsg('O servidor retornou um erro e o usuário não foi cadastrado.');
                    }
               })
     };

     return (
          <div className='modal-criar-aluno'>
               <div className='modal-header'>
                    <h1>Novo Cadastro</h1>
                    <h2>Cadastrar novo {TIPO_USUARIO[tipoUsuario].cargo}</h2>
               </div>

               <div className='modal-main'>

                    <div className='modal-section'>
                         <h1>Informações Pessoais</h1>
                         <div className='informacoes-pessoais'>
                              <div className='dado'>
                                   <p>Nome Completo (*)</p>
                                   <input
                                        type="text"
                                        name="nomeCompleto"
                                        placeholder="Digite o nome completo"
                                        value={payload.nomeCompleto}
                                        onChange={handleChange}
                                   />
                              </div>

                              <div className='dado'>
                                   <p>CPF (*)</p>
                                   <input
                                        type="text"
                                        name="cpf"
                                        placeholder="12345678901"
                                        value={payload.cpf}
                                        onChange={handleChange}
                                   />
                              </div>

                              <div className='dado'>
                                   <p>Data de Nascimento (*)</p>
                                   <input
                                        type="date"
                                        name="dataNascimento"
                                        value={payload.dataNascimento}
                                        onChange={handleChange}
                                   />
                              </div>
                         </div>
                    </div>

                    <div className='modal-section'>
                         <h1>Contato</h1>
                         <div className='contato'>
                              <div className='dado'>
                                   <p>E-mail (*)</p>
                                   <input
                                        type="email"
                                        name="email"
                                        placeholder="E-mail"
                                        value={payload.email}
                                        onChange={handleChange}
                                   />
                              </div>

                              <div className='dado'>
                                   <p>Telefone (*)</p>
                                   <input
                                        type="text"
                                        name="telefone"
                                        placeholder="0011112222"
                                        value={payload.telefone}
                                        onChange={handleChange}
                                   />
                              </div>
                         </div>
                    </div>

                    <div className='modal-section'>
                         <h1>Dados Institucionais</h1>
                         <div className='dados-institucionais'>
                              <div className='dado'>
                                   <p>Matrícula (*)</p>
                                   <input
                                        type="text"
                                        name="matricula"
                                        placeholder="1245789"
                                        value={payload.matricula}
                                        onChange={handleChange}
                                   />
                              </div>
                              <div className='dado'>
                                   <p>Tipo de Usuário (*)</p>
                                   <select
                                        name="idCargo"
                                        value={payload.idCargo}
                                        onChange={handleChange}
                                   >
                                        <option value={2} >Instrutor</option>
                                        <option value={3}>Aluno</option>
                                   </select>
                              </div>
                         </div>
                    </div>

                    <div className='modal-section'>
                         <h1>Senha de Acesso</h1>
                         <p className='confirmarSenhaWarning'>{confirmarSenhaWarning}</p>
                         <div className='senha-de-acesso'>
                              <div className='dado'>
                                   <p>Senha (*)</p>
                                   <input
                                        type="password"
                                        name="senha"
                                        placeholder="Senha"
                                        value={payload.senha}
                                        onChange={handleChange}
                                   />
                              </div>
                              <div className='dado'>
                                   <p>Senha</p>
                                   <input
                                        type="password"
                                        placeholder="Confirmar senha"
                                        onChange={confirmarSenha}
                                   />
                              </div>
                         </div>
                    </div>

                    <p style={{margin:"1rem", color:"red"}}>{msg}</p>
               </div>

               

               <div className='botoes-acao'>
                    <button onClick={() => setIsOpen(false)}>
                         Cancelar
                    </button>

                    <button onClick={submitData}>
                         Cadastrar {TIPO_USUARIO[tipoUsuario].cargo}
                    </button>
               </div>
               {/*<pre>{JSON.stringify(payload, null, 2)}</pre>*/}
          </div>
     );
}

export default CriarAluno;