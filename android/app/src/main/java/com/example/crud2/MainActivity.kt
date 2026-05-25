package com.example.crud2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ProdutoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ajusta o padding pra não ficar embaixo da status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editNome = findViewById<EditText>(R.id.editNome)
        val editPreco = findViewById<EditText>(R.id.editPreco)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)
        val recycler = findViewById<RecyclerView>(R.id.recycler)

        // Configura RecyclerView; ao clicar em "X" no item, deleta
        adapter = ProdutoAdapter { produto -> deletar(produto) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        // Botão adicionar
        btnAdicionar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val preco = editPreco.text.toString().toDoubleOrNull()
            if (nome.isEmpty() || preco == null) {
                Toast.makeText(this, "Preencha nome e preço", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            criar(Produto(nome = nome, preco = preco))
            editNome.text.clear()
            editPreco.text.clear()
        }

        // Ao abrir o app, já carrega a lista
        carregar()
    }

    private fun carregar() {
        lifecycleScope.launch {
            try {
                adapter.atualizar(ApiClient.service.listar())
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun criar(produto: Produto) {
        lifecycleScope.launch {
            try {
                ApiClient.service.criar(produto)
                carregar()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deletar(produto: Produto) {
        lifecycleScope.launch {
            try {
                produto.id?.let { ApiClient.service.deletar(it) }
                carregar()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}