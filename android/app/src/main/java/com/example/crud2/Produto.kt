package com.example.crud2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class Produto(
    val id: Int? = null,
    val nome: String,
    val preco: Double
)

interface ApiService {
    @GET("produtos")
    suspend fun listar(): List<Produto>

    @POST("produtos")
    suspend fun criar(@Body produto: Produto): Produto

    @PUT("produtos/{id}")
    suspend fun atualizar(@Path("id") id: Int, @Body produto: Produto): Produto

    @DELETE("produtos/{id}")
    suspend fun deletar(@Path("id") id: Int): Response<Unit>
}

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val service: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

class ProdutoAdapter(
    private val onEdit: (Produto) -> Unit,
    private val onDelete: (Produto) -> Unit
) : RecyclerView.Adapter<ProdutoAdapter.VH>() {

    private var produtos: List<Produto> = emptyList()

    fun atualizar(novos: List<Produto>) {
        produtos = novos
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNome)
        val txtPreco: TextView = view.findViewById(R.id.txtPreco)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnDeletar: Button = view.findViewById(R.id.btnDeletar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val produto = produtos[position]
        holder.txtNome.text = produto.nome
        holder.txtPreco.text = "R$ %.2f".format(produto.preco)
        holder.btnEditar.setOnClickListener { onEdit(produto) }
        holder.btnDeletar.setOnClickListener { onDelete(produto) }
    }

    override fun getItemCount() = produtos.size
}