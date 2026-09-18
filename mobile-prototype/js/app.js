// FitManager Mobile - interações do protótipo (abrir/fechar popups, alternar abas)

function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('open');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('open');
}

// Fecha o modal ao clicar fora do card
document.addEventListener('click', function (e) {
  if (e.target.classList && e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('open');
  }
});

// Alterna abas simples (Notificações: Todas / Não lidas | Dias da semana)
function showTabPanel(tabButton, panelId, groupName) {
  document.querySelectorAll('[data-tab-group="' + groupName + '"]').forEach(function (btn) {
    btn.classList.remove('active');
  });
  tabButton.classList.add('active');

  document.querySelectorAll('[data-panel-group="' + groupName + '"]').forEach(function (panel) {
    panel.style.display = 'none';
  });
  const panel = document.getElementById(panelId);
  if (panel) panel.style.display = 'block';
}
