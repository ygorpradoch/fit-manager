import "./Confirmar.css"

function Confirmar({ isOpen, title, message, onConfirm, onCancel }) {
     return (
          <div className="confirmar-box">
               <div>
                    <h2>{title}</h2>
                    <p>{message}</p>
                    <div className="confirmar-buttons">
                         <button onClick={onCancel}>
                              Cancelar
                         </button>
                         <button onClick={onConfirm}>
                              Confirmar
                         </button>
                    </div>
               </div>
          </div>
     )
}
export default Confirmar;
