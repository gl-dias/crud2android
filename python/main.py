from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

app = FastAPI(title="API CRUD - Produtos")

app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])


class Produto(BaseModel):
    id: int | None = None
    nome: str = Field(..., min_length=1, description="Nome não pode ser vazio")
    preco: float = Field(..., gt=0, description="Preço deve ser maior que zero")


produtos: list[Produto] = []
proximo_id = 1


@app.get("/produtos")
def listar():
    return produtos


@app.get("/produtos/{produto_id}")
def buscar(produto_id: int):
    for p in produtos:
        if p.id == produto_id:
            return p
    raise HTTPException(404, "Produto não encontrado")


@app.post("/produtos", status_code=201)
def criar(produto: Produto):
    global proximo_id
    if produto.id is not None:
        if any(p.id == produto.id for p in produtos):
            raise HTTPException(409, f"Já existe um produto com id {produto.id}")
        proximo_id = max(proximo_id, produto.id + 1)
    else:
        produto.id = proximo_id
        proximo_id += 1

    produtos.append(produto)
    return produto


@app.put("/produtos/{produto_id}")
def atualizar(produto_id: int, dados: Produto):
    if dados.id is not None and dados.id != produto_id:
        raise HTTPException(
            400,
            f"ID no body ({dados.id}) nao bate com ID na URL ({produto_id})"
        )

    for i, p in enumerate(produtos):
        if p.id == produto_id:
            dados.id = produto_id
            produtos[i] = dados
            return dados
    raise HTTPException(404, "Produto nao encontrado")


@app.delete("/produtos/{produto_id}", status_code=204)
def deletar(produto_id: int):
    for i, p in enumerate(produtos):
        if p.id == produto_id:
            produtos.pop(i)
            return
    raise HTTPException(404, "Produto nao encontrado")