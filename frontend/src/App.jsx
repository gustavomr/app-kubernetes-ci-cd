import { useState, useEffect } from 'react';
import { useFlag } from '@rocketflag/react-sdk';

const API_URL = import.meta.env.VITE_API_URL || '';
const FLAG_ID = 'HDVef5DekXPD9BAiMDZN';

function App() {
  const [produtos, setProdutos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [nome, setNome] = useState('');
  const [valor, setValor] = useState('');

  const { enabled: addEnabled, loading: flagLoading } = useFlag(FLAG_ID);

  useEffect(() => {
    fetch(`${API_URL}/api/produtos`)
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
      })
      .then((data) => {
        setProdutos(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch(`${API_URL}/api/produtos`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome, valor: parseFloat(valor) }),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const novo = await res.json();
      setProdutos((prev) => [...prev, novo]);
      setNome('');
      setValor('');
    } catch (err) {
      alert('Erro ao adicionar produto: ' + err.message);
    }
  };

  if (loading) return <div className="container"><p>Carregando...</p></div>;
  if (error) return <div className="container"><p className="error">Erro: {error}</p></div>;

  return (
    <div className="container">
      <h1>Produtos</h1>

      {flagLoading ? (
        <p className="flag-check">Verificando disponibilidade...</p>
      ) : addEnabled ? (
        <form className="add-form" onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Nome do produto"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            required
          />
          <input
            type="number"
            step="0.01"
            min="0"
            placeholder="Valor"
            value={valor}
            onChange={(e) => setValor(e.target.value)}
            required
          />
          <button type="submit">Adicionar</button>
        </form>
      ) : null}

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Preço</th>
          </tr>
        </thead>
        <tbody>
          {produtos.length === 0 ? (
            <tr>
              <td colSpan="3">Nenhum produto encontrado.</td>
            </tr>
          ) : (
            produtos.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.nome}</td>
                <td>{Number(p.preco).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export default App;
