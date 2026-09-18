import { useEffect, useState } from 'react';
import axios from 'axios';
import "./Modal.css"

const cliente = axios.create({
     baseURL: 'https://fitmanagerapi-production.up.railway.app/api/v1/',
     timeout: 10000,
});

const ROLE_ENDPOINT = {
  2: 'instrutores',
  3: 'alunos',
};

function EditarUsuario({ setIsOpen, reload, usuario }) {
     /* setIsOpen = função de open/close do componente */
     /* usuario = objeto com dados do usuário a ser editado */

     const token = localStorage.getItem('fitmanager_token');

     const [payload, setPayload] = useState({
          nomeCompleto: usuario.nomeCompleto,
          cpf: usuario.cpf,
          dataNascimento: usuario.dataNascimento,
          email: usuario.email,
          telefone: usuario.telefone
     });

     const handleChange = (e) => {
          const { name, value } = e.target;

          setPayload((prev) => ({
               ...prev,
               [name]: value,
          }));
     };

     const submitData = () => {
          const roleSegment = ROLE_ENDPOINT[usuario.cargoId];

          if (!roleSegment) {
               console.error(`Cargo inválido: ${usuario.cargoId}`);
               return;
          }

          const endpoint = `/${roleSegment}/${usuario.id}`;

          cliente
               .put(endpoint, payload, {
                    headers: { Authorization: `Bearer ${token}` },
               })
               .then((response) => {
                    console.log('Usuário atualizado com sucesso:', response.data);
                    setIsOpen(false);
                    reload();
               })
               .catch((error) => {
                    console.error('Erro ao atualizar usuário:', error);
               });
     };

     return (
          <div className='modal-criar-aluno'>
               <div className='modal-header'>
                    <h1>Editar Cadastro</h1>
                    <h2>Atualizar informações do usuário</h2>
               </div>

               <div className='modal-main'>

                    <div className='modal-section'>
                         <h1>Informações Pessoais</h1>
                         <div className='informacoes-pessoais'>
                              <div className='dado'>
                                   <p>Nome Completo</p>
                                   <input
                                        type="text"
                                        name="nomeCompleto"
                                        placeholder="Digite o nome completo"
                                        value={payload.nomeCompleto}
                                        onChange={handleChange}
                                   />
                              </div>

                              <div className='dado'>
                                   <p>CPF</p>
                                   <input
                                        type="text"
                                        name="cpf"
                                        placeholder="000.000.000-00"
                                        value={payload.cpf}
                                        onChange={handleChange}
                                   />
                              </div>

                              <div className='dado'>
                                   <p>Data de Nascimento</p>
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
                                   <p>E-mail</p>
                                   <input
                                        type="email"
                                        name="email"
                                        placeholder="E-mail"
                                        value={payload.email}
                                        onChange={handleChange}
                                   />
                              </div>

                              <div className='dado'>
                                   <p>Telefone</p>
                                   <input
                                        type="text"
                                        name="telefone"
                                        placeholder="Telefone"
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
                                   <p>Matrícula</p>
                                   <input
                                        disabled
                                        type="text"
                                        name="matricula"
                                        placeholder="Matrícula"
                                        value={usuario.matricula}
                                   />
                              </div>
                              <div className='dado'>
                                   <p>Tipo de Usuário</p>
                                   <select
                                        disabled
                                        name="idCargo"
                                        value={usuario.cargoId}
                                   >
                                        <option value={1}>Admin</option>
                                        <option value={2}>Instrutor</option>
                                        <option value={3}>Aluno</option>
                                   </select>
                              </div>
                         </div>
                    </div>
               </div>

               <div className='botoes-acao'>
                    <button onClick={() => setIsOpen(false)}>
                         Cancelar
                    </button>

                    <button onClick={submitData}>
                         Atualizar
                    </button>
               </div>
          </div>
     );
}

export default EditarUsuario;