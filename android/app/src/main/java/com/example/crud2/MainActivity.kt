package com.example.crud2

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editNome = findViewById<EditText>(R.id.editNome)
        val editPreco = findViewById<EditText>(R.id.editPreco)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionar)
        val recycler = findViewById<RecyclerView>(R.id.recycler)

        // Agora o adapter recebe DOIS callbacks: onEdit (abre dialog) e onDelete
        adapter = ProdutoAdapter(
            onEdit = { produto -> mostrarDialogEdicao(produto) },
            onDelete = { produto -> deletar(produto) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

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

    private fun atualizar(id: Int, dados: Produto) {
        lifecycleScope.launch {
            try {
                ApiClient.service.atualizar(id, dados)
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

    /**
     * Abre um AlertDialog com 2 EditText pré-preenchidos para edição.
     * Construído programaticamente para não precisar de outro arquivo XML.
     */
    private fun mostrarDialogEdicao(produto: Produto) {
        val campoNome = EditText(this).apply {
            setText(produto.nome)
            hint = "Nome"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val campoPreco = EditText(this).apply {
            setText(produto.preco.toString())
            hint = "Preço"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)
            addView(campoNome)
            addView(campoPreco)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar produto")
            .setView(layout)
            .setPositiveButton("Salvar") { _, _ ->
                val novoNome = campoNome.text.toString().trim()
                val novoPreco = campoPreco.text.toString().toDoubleOrNull()
                if (novoNome.isEmpty() || novoPreco == null) {
                    Toast.makeText(this, "Preencha nome e preço", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                produto.id?.let { id ->
                    atualizar(id, Produto(nome = novoNome, preco = novoPreco))
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}