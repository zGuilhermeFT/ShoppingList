# Shopping List App

O **Shopping List App** é uma aplicação Android desenvolvida para facilitar a organização e o gerenciamento de suas compras diárias. Com ele, você pode criar múltiplas listas de compras, adicionar itens com detalhes específicos, e muito mais.

## Funcionalidades

### 1. Adicionar Listas de Produtos
- **Descrição**: Permite criar e gerenciar múltiplas listas de compras.
- **Detalhes**:
  - Visualizar todas as listas criadas.
  - Criar listas com nomes personalizados.
  - Adicionar, editar ou remover itens em qualquer lista.
  - Remover ou renomear listas de compras.

### 2. Adicionar Item
- **Descrição**: Capacidade de adicionar novos itens à lista de compras.
- **Detalhes**:
  - Acessível a partir da tela principal quando há uma lista criada (A lista mais recende vem aberta por padrão), caso não haja a tela inicial será a tela de listas.
  - Formulário para inserção do nome, quantidade, escolha de categoria, e status do item (comprado ou não).
  - Botão "Salvar" para adicionar o item à lista.
  - Opção para cancelar a adição do item.

### 3. Visualizar Lista
- **Descrição**: Visualizar todos os itens adicionados às listas de compras.
- **Detalhes**:
  - Itens marcados como comprados são visualmente diferenciados com um checkbox.

### 4. Editar Item
- **Descrição**: Editar informações de qualquer item adicionado.
- **Detalhes**:
  - Tela de edição acessível ao tocar no botão editar de um item.
  - Possibilidade de alterar nome, quantidade, categoria e status.
  - Salvar alterações reflete na lista principal.

### 5. Remover Item
- **Descrição**: Remover itens individuais ou múltiplos de uma lista.
- **Detalhes**:
  - Opção de remoção de itens em lote pelo menu de itens já comprados.
  - Confirmação necessária para evitar exclusões acidentais.

### 6. Marcação de Item como Comprado
- **Descrição**: Marcar itens como comprados de forma fácil e rápida.
- **Detalhes**:
  - Alterar o status do item para "comprado" ao tocar nele ou no editar item.
  - Visualização completa de itens pendentes e comprados mantida.

### 7. Persistência de Dados
- **Descrição**: Todos os dados inseridos são salvos localmente usando SQLite.
- **Detalhes**:
  - Configuração e criação de um banco de dados SQLite.
  - Salvamento automático de alterações.

### 8. Cadastrar Categorias de Produtos
- **Descrição**: Gerenciamento de categorias de produtos.
- **Detalhes**:
  - Adição, edição e remoção de categorias.
  - Associação de itens a categorias na criação.
  - Opções para gerenciar itens ao deletar uma categoria.

## Tecnologias Utilizadas
- Android SDK
- SQLite para armazenamento de dados
