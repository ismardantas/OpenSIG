package br.com.opensig.fiscal.server.sped.fiscal.blocoC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.beanio.BeanWriter;
import org.beanio.StreamFactory;

import br.com.opensig.comercial.shared.modelo.ComEcfNota;
import br.com.opensig.comercial.shared.modelo.ComEcfNotaProduto;
import br.com.opensig.core.server.UtilServer;
import br.com.opensig.fiscal.server.sped.ARegistro;
import br.com.opensig.produto.shared.modelo.ProdIcms;

public class RegistroC300 extends ARegistro<DadosC300, List<ComEcfNota>> {

	private List<DadosC310> lc310 = new ArrayList<DadosC310>();
	private Map<String, List<ComEcfNotaProduto>> analitico = new HashMap<String, List<ComEcfNotaProduto>>();

	@Override
	public void executar() {
		// executa para perfil diferente de A
		if (!auth.getConf().get("sped.fiscal.0000.ind_perfil").equals("A")) {
			try {
				StreamFactory factory = StreamFactory.newInstance();
				factory.load(getClass().getResourceAsStream(bean));
				BeanWriter out = factory.createWriter("EFD", escritor);

				// agrupando as notas por dia+serie+subserie
				Map<String, List<ComEcfNota>> grupo = new HashMap<String, List<ComEcfNota>>();
				for (ComEcfNota nota : notas) {
					String chave = UtilServer.formataData(nota.getComEcfNotaData(), "ddMMyyyy") + nota.getComEcfNotaSerie() + nota.getComEcfNotaSubserie();
					List<ComEcfNota> lista = grupo.get(chave);
					if (lista == null) {
						lista = new ArrayList<ComEcfNota>();
						lista.add(nota);
						grupo.put(chave, lista);
					} else {
						lista.add(nota);
					}
				}

				RegistroC310 r310 = new RegistroC310();
				for (Entry<String, List<ComEcfNota>> entry : grupo.entrySet()) {
					// resumo diario
					bloco = getDados(entry.getValue());
					out.write(bloco);
					out.flush();

					// cancelados
					if (!lc310.isEmpty()) {
						for (DadosC310 d : lc310) {
							r310.setDados(d);
							r310.executar();
							qtdLinhas += r310.getQtdLinhas();
						}
					}

					// analitico diario
					getAnalitico();
				}

			} catch (Exception e) {
				qtdLinhas = 0;
				UtilServer.LOG.error("Erro na geracao do Registro -> " + bean, e);
			}
		}
	}

	@Override
	protected DadosC300 getDados(List<ComEcfNota> dados) throws Exception {
		DadosC300 d = new DadosC300();
		for (ComEcfNota nota : dados) {
			d.setCod_mod("02");
			d.setSer(nota.getComEcfNotaSerie());
			d.setSub(nota.getComEcfNotaSubserie());
			d.setDt_doc(nota.getComEcfNotaData());
			if (nota.getComEcfNotaNumero() < d.getNum_doc_ini()) {
				d.setNum_doc_ini(nota.getComEcfNotaNumero());
			}
			if (nota.getComEcfNotaNumero() > d.getNum_doc_fin()) {
				d.setNum_doc_fin(nota.getComEcfNotaNumero());
			}
			if (!nota.getComEcfNotaCancelada()) {
				d.setVl_doc(somarDoubles(d.getVl_doc(), nota.getComEcfNotaLiquido()));
				d.setVl_pis(somarDoubles(d.getVl_pis(), nota.getComEcfNotaPis()));
				d.setVl_cofins(somarDoubles(d.getVl_cofins(), nota.getComEcfNotaCofins()));
			} else {
				// Registro C310
				DadosC310 c310 = new DadosC310();
				c310.setNum_doc_canc(nota.getComEcfNotaNumero());
				lc310.add(c310);
			}

			// agrupando os itens por cst+cfop+aliq
			for (ComEcfNotaProduto np : nota.getComEcfNotaProdutos()) {
				setAnalitico(np);
			}
		}

		normalizar(d);
		qtdLinhas++;
		return d;
	}

	private void setAnalitico(ComEcfNotaProduto d) {
		ProdIcms icms = d.getProdProduto().getProdIcms();
		String cstCson = auth.getConf().get("nfe.crt").equals("1") ? icms.getProdIcmsCson() : icms.getProdIcmsCst();
		String chave = cstCson + icms.getProdIcmsCfop() + icms.getProdIcmsDentro();
		List<ComEcfNotaProduto> lista = analitico.get(chave);
		if (lista == null) {
			lista = new ArrayList<ComEcfNotaProduto>();
			lista.add(d);
			analitico.put(chave, lista);
		} else {
			lista.add(d);
		}
	}

	private void getAnalitico() {
		if (!analitico.isEmpty()) {
			RegistroC320 r320 = new RegistroC320();
			for (Entry<String, List<ComEcfNotaProduto>> entry2 : analitico.entrySet()) {
				r320.setDados(entry2.getValue());
				r320.executar();
				qtdLinhas += r320.getQtdLinhas();
			}
		}
		analitico.clear();
	}
}
