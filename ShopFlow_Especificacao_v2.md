**ShopFlow**

Plataforma de E-Commerce Completa

*Especificação de Projeto & Guia de Desenvolvimento*

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Stack: Java Spring Boot + React + PostgreSQL</strong></p>
<p>Versão 1.0 | Abril 2026</p></td>
</tr>
</tbody>
</table>

Autor: Patrick Farias

**1. Visão Geral do Projeto**

O ShopFlow é uma plataforma de e-commerce completa desenvolvida como projeto de portfólio. O objetivo é demonstrar dominício das principais tecnologias do mercado --- Java Spring Boot no backend, React no frontend e PostgreSQL como banco de dados relacional --- construindo um sistema real, com regras de negócio autênticas, que possa ser demonstrado em entrevistas técnicas.

|                        |                                      |
|------------------------|--------------------------------------|
| **Nome do Projeto**    | ShopFlow                             |
| **Tipo**               | Plataforma de E-Commerce (Portfólio) |
| **Backend**            | Java 21 + Spring Boot 3.x            |
| **Frontend**           | React 18 + TypeScript                |
| **Banco de Dados**     | PostgreSQL 16                        |
| **Autenticação**       | JWT (Spring Security)                |
| **Controle de Versão** | Git / GitHub                         |
| **Autor**              | Patrick Farias                       |
| **Data de Início**     | Abril 2026                           |

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>🎯 Objetivo de Portfólio</strong></p>
<p>Este projeto será publicado no GitHub e utilizado como material de demonstração em entrevistas. Priorize clareza de código, boas práticas de arquitetura e cobertura de testes básicos.</p></td>
</tr>
</tbody>
</table>

**2. Arquitetura do Sistema**

O projeto adota uma arquitetura cliente-servidor desacoplada: o backend expõe uma API REST e o frontend consome essa API de forma independente. Os serviços se comunicam via HTTP/JSON com autenticação por token JWT.

**2.1 Diagrama de Camadas (Backend)**

O backend segue a arquitetura em camadas clássica do Spring Boot. A coluna \'Tipo\' indica se a camada é implementada como interface Java, classe concreta ou ambas:

|            |             |                               |                                                                 |
|------------|-------------|-------------------------------|-----------------------------------------------------------------|
| **Camada** | **Pacote**  | **Tipo**                      | **Responsabilidade**                                            |
| Controller | controller/ | Classe (@RestController)      | Recebe requisições HTTP, valida entrada, retorna respostas      |
| Service    | service/    | Interface + Classe (@Service) | Define o contrato e contém as regras de negócio                 |
| Repository | repository/ | Interface (JpaRepository)     | Acesso ao banco --- Spring gera a implementação automaticamente |
| Entity     | entity/     | Classe (@Entity)              | Mapeamento das tabelas do PostgreSQL (JPA/Hibernate)            |
| DTO        | dto/        | Classe (record ou POJO)       | Objetos de transferência de dados (request / response)          |
| Security   | security/   | Classe + Interface            | Configuração JWT, filtros e controle de acesso                  |
| Exception  | exception/  | Classe (@ControllerAdvice)    | Tratamento global de erros padronizados                         |

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Sobre o uso de Interfaces nos Services</strong></p>
<p>O padrão adotado neste projeto é: para cada Service, criar uma interface que define o contrato e uma classe de implementação que carrega a anotação @Service. Exemplo: ProductService (interface) e ProductServiceImpl (implementação). Isso segue o padrão enterprise Java clássico, ainda muito cobrado em entrevistas, e deixa o código mais flexível para testes com Mockito.</p></td>
</tr>
</tbody>
</table>

**Exemplo de como fica na prática:**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Interface — ProductService.java</strong></p>
<p>public interface ProductService {
Page&lt;ProductResponse&gt; findAll(Pageable pageable, Long categoryId);
ProductResponse findById(Long id);
ProductResponse create(ProductRequest request);
ProductResponse update(Long id, ProductRequest request);
void delete(Long id);
}</p></td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Implementação — ProductServiceImpl.java</strong></p>
<p>@Service
@RequiredArgsConstructor // Lombok injeta o repositório via construtor
public class ProductServiceImpl implements ProductService {
private final ProductRepository productRepository;
private final CategoryRepository categoryRepository;
@Override
public Page&lt;ProductResponse&gt; findAll(Pageable pageable, Long categoryId) {
// lógica de negócio aqui
}
// ... demais métodos
}</p></td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Repository — ProductRepository.java</strong></p>
<p>// Apenas interface — o Spring Data JPA gera a implementação em tempo de execução
public interface ProductRepository extends JpaRepository&lt;Product, Long&gt; {
Page&lt;Product&gt; findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
Optional&lt;Product&gt; findByIdAndActiveTrue(Long id);
}</p></td>
</tr>
</tbody>
</table>

**Resumo rápido:**

- Repository → sempre interface (Spring Data JPA gera a implementação)

- Service → interface (contrato) + classe com @Service (implementação)

- Controller → sempre classe concreta com @RestController

- Entity / DTO → sempre classes simples (com Lombok para reduzir boilerplate)

**2.2 Estrutura de Pastas Sugerida**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Backend — Spring Boot</strong></p>
<p>src/main/java/com/shopflow/
├─ controller/ → REST Controllers
├─ service/ → Business Logic
├─ repository/ → JPA Repositories
├─ entity/ → JPA Entities
├─ dto/ → DTOs (request/response)
├─ security/ → JWT Config &amp; Filters
├─ exception/ → Global Error Handler
└─ config/ → CORS, Beans, etc.</p></td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Frontend — React</strong></p>
<p>src/
├─ components/ → Componentes reutilizáveis
├─ pages/ → Páginas da aplicação
├─ services/ → Chamadas à API (axios)
├─ context/ → Estado global (React Context / Zustand)
├─ hooks/ → Custom hooks
├─ types/ → TypeScript interfaces
└─ router/ → Rotas (React Router)</p></td>
</tr>
</tbody>
</table>

**3. Módulos e Funcionalidades**

**3.1 Autenticação e Autorização**

Sistema de autenticação baseado em JWT com dois perfis de usuário: cliente (ROLE_CUSTOMER) e administrador (ROLE_ADMIN).

- Registro de novo usuário (nome, email, senha com hash BCrypt)

- Login com retorno de JWT e refresh token

- Logout com invalidação de token (blacklist em memória ou Redis opcional)

- Rotas protegidas por role via Spring Security

- Validação de email único no cadastro

**3.2 Catálogo de Produtos**

Módulo públíco (sem autenticação) para navegação de produtos. Administradores têm acesso às operações de escrita.

- Listagem de produtos com paginação (Spring Pageable)

- Filtro por categoria, faixa de preço e nome

- Detalhes do produto: nome, descrição, preço, imagem, estoque, categoria

- CRUD completo de produtos (admin)

- CRUD de categorias (admin)

- Upload de imagem de produto (armazenamento local ou AWS S3 --- opcional na fase avançada)

**3.3 Carrinho de Compras**

O carrinho é persistido no banco de dados vinculado ao usuário autenticado, garantindo persistência entre sessões.

- Adicionar produto ao carrinho (com validação de estoque)

- Atualizar quantidade de item

- Remover item do carrinho

- Limpar carrinho

- Visualizar resumo do carrinho com subtotal calculado

- Validação: não permitir quantidade maior que estoque disponível

**3.4 Gestão de Pedidos**

Fluxo completo de pedido desde a finalização do carrinho até a entrega, com estados bem definidos.

- Finalizar compra (checkout) a partir do carrinho ativo

- Registro do endereço de entrega no pedido

- Simulação de pagamento (campo status: PENDING / PAID / FAILED)

- Histórico de pedidos do usuário

- Detalhes do pedido com itens e valores

- Atualização de status pelo administrador

Estados do pedido:

|            |                                     |                      |
|------------|-------------------------------------|----------------------|
| **Status** | **Descrição**                       | **Quem Atualiza**    |
| PENDING    | Pedido criado, aguardando pagamento | Sistema (automático) |
| PAID       | Pagamento confirmado                | Sistema / Admin      |
| PROCESSING | Em preparação para envio            | Admin                |
| SHIPPED    | Enviado ao cliente                  | Admin                |
| DELIVERED  | Entregue ao cliente                 | Admin                |
| CANCELLED  | Pedido cancelado                    | Cliente / Admin      |

**3.5 Controle de Estoque**

O estoque é gerenciado de forma integrada ao fluxo de pedidos, garantindo consistência dos dados.

- Estoque atual vinculado a cada produto

- Decremento automático ao confirmar pedido

- Estorno de estoque ao cancelar pedido

- Alerta de estoque baixo (campo de threshold por produto)

- Gestão manual de estoque pelo admin (entrada de mercadoria)

- Histórico de movimentações de estoque (StockMovement)

**3.6 Painel Administrativo**

Interface exclusiva para usuários com ROLE_ADMIN, acessível em rota separada no frontend.

- Dashboard com métricas: total de pedidos, receita do dia, produtos com estoque baixo

- Listagem e gestão de produtos e categorias

- Visualização e atualização de status de pedidos

- Gestão de usuários (listagem, desativação de conta)

- Ajuste de estoque manual

**4. Modelo de Dados (PostgreSQL)**

O banco de dados segue um modelo relacional normalizado. Abaixo estão as principais entidades e seus atributos.

**4.1 Tabela: users**

|            |              |                                |                          |
|------------|--------------|--------------------------------|--------------------------|
| **Coluna** | **Tipo**     | **Constraint**                 | **Descrição**            |
| id         | BIGSERIAL    | PRIMARY KEY                    | Identificador único      |
| name       | VARCHAR(100) | NOT NULL                       | Nome completo            |
| email      | VARCHAR(150) | NOT NULL, UNIQUE               | Email de acesso          |
| password   | VARCHAR(255) | NOT NULL                       | Senha (BCrypt hash)      |
| role       | VARCHAR(20)  | NOT NULL, DEFAULT \'CUSTOMER\' | Perfil: CUSTOMER / ADMIN |
| active     | BOOLEAN      | NOT NULL, DEFAULT true         | Conta ativa?             |
| created_at | TIMESTAMP    | NOT NULL                       | Data de cadastro         |

**4.2 Tabela: categories**

|             |             |                  |                   |
|-------------|-------------|------------------|-------------------|
| **Coluna**  | **Tipo**    | **Constraint**   | **Descrição**     |
| id          | BIGSERIAL   | PRIMARY KEY      | Identificador     |
| name        | VARCHAR(80) | NOT NULL, UNIQUE | Nome da categoria |
| description | TEXT        |                  | Descrição         |
| active      | BOOLEAN     | DEFAULT true     | Ativa?            |

**4.3 Tabela: products**

|                 |               |                     |                         |
|-----------------|---------------|---------------------|-------------------------|
| **Coluna**      | **Tipo**      | **Constraint**      | **Descrição**           |
| id              | BIGSERIAL     | PRIMARY KEY         | Identificador           |
| name            | VARCHAR(150)  | NOT NULL            | Nome do produto         |
| description     | TEXT          |                     | Descrição detalhada     |
| price           | NUMERIC(10,2) | NOT NULL            | Preço unitário          |
| stock_qty       | INTEGER       | NOT NULL, DEFAULT 0 | Quantidade em estoque   |
| stock_threshold | INTEGER       | DEFAULT 5           | Alerta de estoque baixo |
| image_url       | VARCHAR(300)  |                     | URL da imagem           |
| category_id     | BIGINT        | FK categories       | Categoria do produto    |
| active          | BOOLEAN       | DEFAULT true        | Produto ativo?          |
| created_at      | TIMESTAMP     | NOT NULL            | Data de cadastro        |

**4.4 Tabelas: carts e cart_items**

|                |               |                  |                            |
|----------------|---------------|------------------|----------------------------|
| **Coluna**     | **Tipo**      | **Constraint**   | **Descrição**              |
| id (cart)      | BIGSERIAL     | PRIMARY KEY      | Identificador do carrinho  |
| user_id        | BIGINT        | FK users, UNIQUE | Um carrinho por usuário    |
| created_at     | TIMESTAMP     | NOT NULL         | Data de criação            |
| id (cart_item) | BIGSERIAL     | PRIMARY KEY      | Identificador do item      |
| cart_id        | BIGINT        | FK carts         | Referência ao carrinho     |
| product_id     | BIGINT        | FK products      | Produto adicionado         |
| quantity       | INTEGER       | NOT NULL         | Quantidade desejada        |
| unit_price     | NUMERIC(10,2) | NOT NULL         | Preço no momento da adição |

**4.5 Tabelas: orders e order_items**

|                 |               |                |                         |
|-----------------|---------------|----------------|-------------------------|
| **Coluna**      | **Tipo**      | **Constraint** | **Descrição**           |
| id (order)      | BIGSERIAL     | PRIMARY KEY    | Identificador do pedido |
| user_id         | BIGINT        | FK users       | Comprador               |
| status          | VARCHAR(20)   | NOT NULL       | Status atual do pedido  |
| total_amount    | NUMERIC(10,2) | NOT NULL       | Valor total             |
| address         | TEXT          | NOT NULL       | Endereço de entrega     |
| created_at      | TIMESTAMP     | NOT NULL       | Data do pedido          |
| id (order_item) | BIGSERIAL     | PRIMARY KEY    | Item do pedido          |
| order_id        | BIGINT        | FK orders      | Pedido pai              |
| product_id      | BIGINT        | FK products    | Produto                 |
| quantity        | INTEGER       | NOT NULL       | Quantidade              |
| unit_price      | NUMERIC(10,2) | NOT NULL       | Preço à época da compra |

**4.6 Tabela: stock_movements**

|            |              |                 |                               |
|------------|--------------|-----------------|-------------------------------|
| **Coluna** | **Tipo**     | **Constraint**  | **Descrição**                 |
| id         | BIGSERIAL    | PRIMARY KEY     | Identificador                 |
| product_id | BIGINT       | FK products     | Produto afetado               |
| type       | VARCHAR(20)  | NOT NULL        | IN (entrada) / OUT (saída)    |
| quantity   | INTEGER      | NOT NULL        | Quantidade movimentada        |
| reason     | VARCHAR(150) |                 | Motivo (pedido, ajuste, etc.) |
| order_id   | BIGINT       | FK orders, NULL | Pedido origem (se aplicável)  |
| created_at | TIMESTAMP    | NOT NULL        | Data da movimentação          |

**5. Endpoints da API REST**

Base URL: http://localhost:8080/api/v1

**5.1 Autenticação**

|            |                |          |                        |
|------------|----------------|----------|------------------------|
| **Método** | **Endpoint**   | **Auth** | **Descrição**          |
| POST       | /auth/register | Público  | Registrar novo usuário |
| POST       | /auth/login    | Público  | Login e retorno de JWT |
| POST       | /auth/refresh  | Público  | Renovar access token   |
| POST       | /auth/logout   | Token    | Invalidar token        |

**5.2 Produtos**

|            |                      |          |                                      |
|------------|----------------------|----------|--------------------------------------|
| **Método** | **Endpoint**         | **Auth** | **Descrição**                        |
| GET        | /products            | Público  | Listar produtos (paginado + filtros) |
| GET        | /products/{id}       | Público  | Detalhes de um produto               |
| POST       | /products            | Admin    | Criar produto                        |
| PUT        | /products/{id}       | Admin    | Atualizar produto                    |
| DELETE     | /products/{id}       | Admin    | Desativar produto                    |
| GET        | /products/categories | Público  | Listar categorias                    |
| POST       | /products/categories | Admin    | Criar categoria                      |

**5.3 Carrinho**

|            |                  |          |                              |
|------------|------------------|----------|------------------------------|
| **Método** | **Endpoint**     | **Auth** | **Descrição**                |
| GET        | /cart            | Token    | Visualizar carrinho ativo    |
| POST       | /cart/items      | Token    | Adicionar item ao carrinho   |
| PUT        | /cart/items/{id} | Token    | Atualizar quantidade de item |
| DELETE     | /cart/items/{id} | Token    | Remover item do carrinho     |
| DELETE     | /cart            | Token    | Limpar carrinho              |

**5.4 Pedidos**

|            |                     |          |                                 |
|------------|---------------------|----------|---------------------------------|
| **Método** | **Endpoint**        | **Auth** | **Descrição**                   |
| POST       | /orders             | Token    | Finalizar compra (checkout)     |
| GET        | /orders             | Token    | Histórico de pedidos do usuário |
| GET        | /orders/{id}        | Token    | Detalhes de um pedido           |
| PATCH      | /orders/{id}/status | Admin    | Atualizar status do pedido      |
| GET        | /admin/orders       | Admin    | Todos os pedidos (admin view)   |

**5.5 Estoque**

|            |                                  |          |                            |
|------------|----------------------------------|----------|----------------------------|
| **Método** | **Endpoint**                     | **Auth** | **Descrição**              |
| GET        | /admin/stock                     | Admin    | Visão geral de estoque     |
| POST       | /admin/stock/{productId}/adjust  | Admin    | Ajuste manual de estoque   |
| GET        | /admin/stock/{productId}/history | Admin    | Histórico de movimentações |

**6. Stack Tecnológica Detalhada**

**6.1 Backend --- Java Spring Boot**

|                   |            |                               |
|-------------------|------------|-------------------------------|
| **Tecnologia**    | **Versão** | **Finalidade**                |
| Java              | 21 (LTS)   | Linguagem principal           |
| Spring Boot       | 3.2+       | Framework base                |
| Spring Security   | 6.x        | Autenticação e autorização    |
| Spring Data JPA   | 3.x        | ORM e acesso ao banco         |
| Hibernate         | 6.x        | Implementação JPA             |
| jjwt (JWT)        | 0.12+      | Geração e validação de tokens |
| Lombok            | 1.18+      | Redução de boilerplate        |
| MapStruct         | 1.5+       | Mapeamento Entity ↔ DTO       |
| Flyway            | 9.x+       | Migrations de banco de dados  |
| Maven             | 3.9+       | Gerenciamento de dependências |
| JUnit 5 + Mockito | Incluído   | Testes unitários              |

**6.2 Frontend --- React**

|                 |                |                                              |
|-----------------|----------------|----------------------------------------------|
| **Tecnologia**  | **Versão**     | **Finalidade**                               |
| React           | 18.x           | Framework UI                                 |
| TypeScript      | 5.x            | Tipagem estática                             |
| React Router    | 6.x            | Navegação entre páginas                      |
| Axios           | 1.x            | Chamadas à API REST                          |
| React Query     | 5.x (TanStack) | Cache e estado de servidor                   |
| Zustand         | 4.x            | Estado global (alternativa simples ao Redux) |
| Tailwind CSS    | 3.x            | Estilização rápida e responsiva              |
| React Hook Form | 7.x            | Gerenciamento de formulários                 |
| Zod             | 3.x            | Validação de schema                          |
| Vite            | 5.x            | Build tool e dev server                      |

**6.3 Banco de Dados e Infra**

|                    |                                            |
|--------------------|--------------------------------------------|
| **Tecnologia**     | **Finalidade**                             |
| PostgreSQL 16      | Banco de dados relacional principal        |
| Docker Compose     | Subir PostgreSQL localmente sem instalação |
| DBeaver / pgAdmin  | Cliente visual para o banco de dados       |
| GitHub             | Controle de versão e portfólio             |
| Swagger / OpenAPI  | Documentação automática da API (springdoc) |
| Insomnia / Postman | Teste manual dos endpoints                 |

**7. Roadmap de Desenvolvimento**

O projeto é dividido em 5 fases progressivas. Cada fase entrega funcionalidade rodando de ponta a ponta, permitindo aprendizado incremental sem frustração.

**Fase 1 --- Fundação (Backend + Banco)**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Conceitos aprendidos</strong></p>
<p>Spring Boot project setup, Maven, configuração do DataSource, JPA Entities, Spring Data Repositories, Flyway migrations, PostgreSQL.</p></td>
</tr>
</tbody>
</table>

1.  Criar projeto Spring Boot via Spring Initializr

2.  Configurar Docker Compose com PostgreSQL

3.  Criar migration Flyway inicial (tabelas users, categories, products)

4.  Implementar entidades JPA e repositórios

5.  Criar endpoints GET de produtos (sem auth) e testar no Postman

**Fase 2 --- Autenticação JWT**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Conceitos aprendidos</strong></p>
<p>Spring Security, BCrypt, filtros HTTP, JWT (geração e validação), roles e autorização por endpoint.</p></td>
</tr>
</tbody>
</table>

6.  Configurar Spring Security (desabilitar CSRF para API REST)

7.  Implementar registro e login com BCrypt

8.  Gerar e validar tokens JWT com a biblioteca jjwt

9.  Criar filtro JwtAuthFilter e configurar SecurityFilterChain

10. Proteger endpoints de admin com @PreAuthorize

**Fase 3 --- Carrinho e Pedidos**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Conceitos aprendidos</strong></p>
<p>Transações JPA (@Transactional), relacionamentos entre entidades, lógica de negócio complexa, atualização de estoque integrada.</p></td>
</tr>
</tbody>
</table>

11. Criar entidades Cart, CartItem, Order, OrderItem

12. Implementar CRUD do carrinho com validação de estoque

13. Implementar fluxo de checkout (cart → order + decremento de estoque)

14. Implementar histórico de pedidos do usuário

15. Implementar atualização de status de pedido (admin)

**Fase 4 --- Frontend React**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Conceitos aprendidos</strong></p>
<p>React Router, Axios com interceptors JWT, React Query para cache, Zustand para estado global, formulários com React Hook Form + Zod, Tailwind CSS.</p></td>
</tr>
</tbody>
</table>

16. Setup do projeto React com Vite + TypeScript + Tailwind

17. Criar páginas: Home, Catálogo, Detalhe do Produto, Login, Registro

18. Implementar fluxo de autenticação (interceptor Axios com JWT)

19. Criar páginas do carrinho e checkout

20. Criar página de histórico de pedidos

21. Criar painel administrativo (rotas protegidas por role)

**Fase 5 --- Polimento e Diferenciais**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Diferenciais para portfólio</strong></p>
<p>Testes, documentação de API e alguns extras deixam o projeto muito acima da média para entrevistas.</p></td>
</tr>
</tbody>
</table>

22. Documentação automática com Swagger (springdoc-openapi)

23. Testes unitários nos Services com JUnit 5 + Mockito

24. Testes de integração nos Controllers com @SpringBootTest

25. Tratamento global de erros com @ControllerAdvice e respostas padronizadas

26. Paginação e ordenação nos endpoints de listagem

27. README completo no GitHub com prints e instruções de execução

28. (Opcional) Upload de imagem de produto

29. (Opcional) Dashboard com gráficos de vendas

**8. Configuração do Ambiente de Desenvolvimento**

**8.1 Pré-requisitos**

- JDK 21 --- https://adoptium.net

- Node.js 20+ --- https://nodejs.org

- Docker Desktop --- https://docker.com

- VS Code ou IntelliJ IDEA

- Git --- https://git-scm.com

**8.2 Docker Compose (PostgreSQL)**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>docker-compose.yml</strong></p>
<p>version: '3.8'
services:
postgres:
image: postgres:16
environment:
POSTGRES_DB: shopflow
POSTGRES_USER: shopflow_user
POSTGRES_PASSWORD: shopflow_pass
ports:
- '5432:5432'
volumes:
- pgdata:/var/lib/postgresql/data
volumes:
pgdata:</p></td>
</tr>
</tbody>
</table>

**8.3 application.properties (Spring Boot)**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>src/main/resources/application.properties</strong></p>
<p>spring.datasource.url=jdbc:postgresql://localhost:5432/shopflow
spring.datasource.username=shopflow_user
spring.datasource.password=shopflow_pass
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
jwt.secret=sua-chave-secreta-aqui-min-256bits
jwt.expiration=86400000</p></td>
</tr>
</tbody>
</table>

**8.4 Comandos de Execução**

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Backend</strong></p>
<p># Subir o banco
docker-compose up -d
# Rodar o Spring Boot
mvn spring-boot:run
# Acessar Swagger
http://localhost:8080/swagger-ui.html</p></td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>Frontend</strong></p>
<p># Instalar dependências
npm install
# Rodar em desenvolvimento
npm run dev
# Acessar
http://localhost:5173</p></td>
</tr>
</tbody>
</table>

**9. Boas Práticas e Padrões Adotados**

- Nunca expor a entidade JPA diretamente na API --- sempre usar DTOs

- Senhas armazenadas com BCrypt (nunca em texto puro)

- Validação de entrada com Bean Validation (@Valid, @NotNull, @Size, etc.)

- Respostas de erro padronizadas: { timestamp, status, error, message, path }

- Controle de transações com @Transactional nos Services

- Commits no padrão Conventional Commits: feat:, fix:, chore:, docs:

- Branches: main (estável), develop (integração), feature/\* (desenvolvimento)

- Variáveis sensíveis via .env / application-local.properties (não commitar)

- CORS configurado para aceitar apenas o frontend local em desenvolvimento

**10. Próximos Passos**

Com este documento em mãos, o caminho sugerido é:

30. Criar o repositório no GitHub com o nome \'shopflow\'

31. Iniciar o projeto Spring Boot com as dependências da Fase 1

32. Configurar o Docker Compose e validar a conexão com o banco

33. Criar a primeira migration Flyway e as entidades JPA

34. Implementar os endpoints públicos de produtos e testar no Postman

35. Seguir o roadmap fase a fase, commitando ao final de cada funcionalidade

<table>
<colgroup>
<col style="width: 100%" />
</colgroup>
<tbody>
<tr class="odd">
<td><p><strong>💡 Dica Final</strong></p>
<p>Não tente implementar tudo de uma vez. Cada fase deve resultar em algo funcionando de ponta a ponta. Isso mantém a motivação alta e garante que você sempre tenha algo para demonstrar.</p></td>
</tr>
</tbody>
</table>
