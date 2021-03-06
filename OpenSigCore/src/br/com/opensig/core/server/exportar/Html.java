package br.com.opensig.core.server.exportar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import br.com.opensig.core.client.controlador.filtro.FiltroObjeto;
import br.com.opensig.core.client.servico.CoreException;
import br.com.opensig.core.client.servico.CoreService;
import br.com.opensig.core.server.UtilServer;
import br.com.opensig.core.shared.modelo.Dados;
import br.com.opensig.core.shared.modelo.ExpListagem;
import br.com.opensig.core.shared.modelo.ExpMeta;
import br.com.opensig.core.shared.modelo.ExpRegistro;
import br.com.opensig.core.shared.modelo.sistema.SisExpImp;

/**
 * Classe que define a exportacao de arquivo no formato de HTML.
 * 
 * @author Pedro H. Lira
 */
public class Html<E extends Dados> extends AExportacao<E> {

	@Override
	public String getArquivo(CoreService<E> service, SisExpImp modo, ExpListagem<E> exp, String[][] enderecos, String[][] contatos) {
		this.agrupados = new double[exp.getMetadados().size()];
		this.expLista = exp;
		String path = UtilServer.PATH_EMPRESA + "tmp/" + new Date().getTime() + ".html";

		try {
			bw = new BufferedWriter(new FileWriter(path));
			// inicio do arquivo
			bw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html xmlns='http://www.w3.org/1999/xhtml'>");
			// estilo do arquivo
			formato = "landscape";
			bw.write(getEstilo(exp.getNome()));
			// cabecalho da empresa
			bw.write(getCabecalhoEmpresa());
			// inicio da listagem
			bw.write("<table -fs-table-paginate: paginate;>");
			// cabeçalho da listagem
			bw.write(getCabecalhoListagem());
			// corpo da listagem
			bw.write("<tbody>");
			bw.flush();
			// seleciona os dados
			int inicio = modo.getInicio();
			int limite = modo.getLimite() == 0 || modo.getLimite() > PAGINACAO ? PAGINACAO : modo.getLimite();
			int fim = 0;
			do {
				lista = service.selecionar(exp.getClasse(), inicio, limite, exp.getFiltro(), true);
				// determina o fim do recorte
				if (lista.getTotal() - inicio < limite) {
					fim = lista.getTotal() - inicio;
				} else {
					fim = limite;
				}
				bw.write(getCorpoListagem(fim));
				bw.flush();
				inicio += limite;
			} while (fim == PAGINACAO && (modo.getLimite() == 0 || modo.getLimite() > PAGINACAO));
			bw.write("</tbody>");
			// rodape da listagem
			bw.write(getRodapeListagem());
			// fim da listagem
			bw.write("</table>");
			// rodape da empresa
			bw.write(getRodapeEmpresa(enderecos, contatos));
			// fim do arquivo
			bw.write("</body></html>");
			
			bw.flush();
			bw.close();
			return path;
		} catch (IOException ex) {
			return null;
		} catch (CoreException e) {
			return null;
		}
	}

	@Override
	public String getArquivo(CoreService<E> service, SisExpImp modo, ExpRegistro<E> exp, String[][] enderecos, String[][] contatos) {
		this.expReg = exp;
		String path = UtilServer.PATH_EMPRESA + "tmp/" + new Date().getTime() + ".html";

		try {
			bw = new BufferedWriter(new FileWriter(path));
			// inicio do arquivo
			bw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html xmlns='http://www.w3.org/1999/xhtml'>");
			// estilo do arquivo
			formato = exp.getExpLista() == null ? "portrait" : "landscape";
			bw.write(getEstilo(exp.getNome()));
			// cabecalho da empresa
			bw.write(getCabecalhoEmpresa());
			bw.flush();
			// seleciona os dados
			int inicio = modo.getInicio();
			int limite = modo.getLimite() == 0 || modo.getLimite() > PAGINACAO ? PAGINACAO : modo.getLimite();
			int fim = 0;
			do {
				registro = service.selecionar(exp.getClasse(), inicio, limite, exp.getFiltro(), true);
				// determina o fim do recorte
				if (registro.getTotal() - inicio < limite) {
					fim = registro.getTotal() - inicio;
				} else {
					fim = limite;
				}
				bw.write(getCorpoRegistro(service, fim));
				bw.flush();
				inicio += limite;
			} while (fim == PAGINACAO && (modo.getLimite() == 0 || modo.getLimite() > PAGINACAO));
			// rodape da empresa
			bw.write(getRodapeEmpresa(enderecos, contatos));
			// fim do arquivo
			bw.write("</body></html>");
			
			bw.flush();
			bw.close();
			return path;
		} catch (IOException e) {
			return null;
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * Metodo que gera o cabecalho do registro.
	 * 
	 * @return o cabecalho do registro.
	 */
	public String getCabecalhoRegistro() {
		String cabecalho = "<caption>:: " + expReg.getNome() + " ::</caption>";
		return cabecalho;
	}

	/**
	 * Metodo que gera o corpo do registro.
	 * 
	 * @return o corpo do registro.
	 */
	public String getCorpoRegistro(CoreService<E> service, int fim) {
		StringBuffer sb = new StringBuffer();
		for (int pos = 0; pos < fim; pos++) {
			String[] reg = registro.getDados()[pos];
			// inicio do registro
			sb.append("<table>");
			// cabeçalho do registro
			sb.append(getCabecalhoRegistro());
			// corpo do registro
			sb.append(getCorpoRegistro(reg));
			// fim do registro
			sb.append("</table>");
			// listas do registro
			if (expReg.getExpLista() != null) {
				for (ExpListagem aux : expReg.getExpLista()) {
					// seleciona os dados
					if (aux.getFiltro() instanceof FiltroObjeto) {
						Dados d = (Dados) aux.getFiltro().getValor();
						int id = d.getCampoId().equalsIgnoreCase("empEntidadeId") ? Integer.valueOf(reg[1]) : Integer.valueOf(reg[0]);
						d.setId(id);
					}
					try {
						lista = service.selecionar(aux.getClasse(), 0, 0, aux.getFiltro(), true);
					} catch (CoreException e) {
						return "";
					}
					agrupados = new double[aux.getMetadados().size()];
					expLista = aux;
					// inicio listagem
					sb.append("<hr /><table>");
					// cabecalho da listagem
					sb.append(getCabecalhoListagem());
					// corpo da listagem
					sb.append(getCorpoListagem(lista.getTotal()));
					// rodape da listagem
					sb.append(getRodapeListagem());
					// fim da listagem
					sb.append("</table>");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Metodo que gera o corpo do registro.
	 * 
	 * @return o corpo do registro.
	 */
	public String getCorpoRegistro(String[] dados) {
		StringBuffer sb = new StringBuffer("<tbody><tr>");
		int col = 0;
		int visivel = 0;

		for (ExpMeta meta : expReg.getMetadados()) {
			if (meta != null) {
				sb.append("<td><div class=\"nobreak\"><b>" + meta.getRotulo() + "</b>: " + getValor(dados[col]) + "</div></td>");
				visivel++;
				if (visivel != 0 && visivel % 4 == 0) {
					sb.append("</tr><tr>");
				}
			}
			col++;
		}

		int rest = 4 - (visivel % 4);
		if (rest != 4) {
			sb.append("<td colspan='" + rest + "'><div class=\"nobreak\">&nbsp;</div></td>");
		}
		sb.append("</tr></tbody>");
		return sb.toString();
	}

	/**
	 * Metodo que gera o cabecalho da exportacao com os dados da empresa.
	 * 
	 * @return o cabecalho da exportacao.
	 */
	public String getCabecalhoEmpresa() {
		// dados da empresa
		StringBuffer sb = new StringBuffer("<table><tbody><tr style='height: 10px;'>");
		sb.append("<td>" + auth.getEmpresa()[2] + "</td>");
		sb.append("<td align='right'>" + auth.getConf().get("txtData") + " :: " + UtilServer.formataData(new Date(), DateFormat.MEDIUM) + " " + UtilServer.formataHora(new Date(), DateFormat.MEDIUM)
				+ "</td></tr>");
		sb.append("<tr style='height: 10px;'><td>" + auth.getConf().get("txtEntidadeDoc1") + ": " + auth.getEmpresa()[5] + " " + auth.getConf().get("txtEntidadeDoc2") + ": " + auth.getEmpresa()[6]
				+ "</td>");
		sb.append("<td align='right'>" + auth.getConf().get("txtUsuario") + " :: " + auth.getUsuario()[1] + "</td></tr>");
		// finalizando
		sb.append("</tbody></table><hr />");
		return sb.toString();
	}

	/**
	 * Metodo que gera o rodape da exportacao com os dados da empresa.
	 * 
	 * @param enderecos
	 *            os dados dos enderecos.
	 * @param contatos
	 *            os dados dos contatos.
	 * @return o rodape da exportacao.
	 */
	public String getRodapeEmpresa(String[][] enderecos, String[][] contatos) {
		// dados do endereco
		StringBuffer sbEndereco = new StringBuffer("<table><tbody>");
		for (String[] endereco : enderecos) {
			sbEndereco.append("<tr style='height: 10px;'>");
			sbEndereco.append("<td style='width:50px'>" + endereco[2] + "::</td>");
			sbEndereco.append("<td>" + endereco[7] + ", " + endereco[8] + "  " + endereco[9] + " " + endereco[10] + " " + endereco[11] + " - " + endereco[3] + " " + endereco[4] + " " + endereco[6]
					+ "</td>");
			sbEndereco.append("</tr>");
		}
		sbEndereco.append("</tbody></table>");
		// dados do contato
		StringBuffer sbContato = new StringBuffer("<table><tbody>");
		for (String[] contato : contatos) {
			sbContato.append("<tr style='height: 10px;'>");
			sbContato.append("<td align='right'>" + contato[2] + "::</td>");
			sbContato.append("<td>" + contato[3] + "</td>");
			sbContato.append("</tr>");
		}
		sbContato.append("</tbody></table>");
		// alinhamento
		StringBuffer sb = new StringBuffer("<hr /><table><tbody><tr style='height: 10px;'>");
		sb.append("<td style='width:70%'>" + sbEndereco.toString() + "</td>");
		sb.append("<td style='width:30%'>" + sbContato.toString() + "</td>");
		sb.append("</tr></tbody></table>");

		return sb.toString();
	}

	/**
	 * Metodo que gera os estilos usados pela exportacao.
	 * 
	 * @param titulo
	 *            o titulo da pagina.
	 * @return o estilo usado.
	 */
	public String getEstilo(String titulo) {
		StringBuffer sb = new StringBuffer("<head>");
		sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></meta>");
		sb.append("<style type=\"text/css\" media=\"all\">");
		sb.append("@page {size: " + formato + "; margin: 0.25in; @bottom-center { content: \"Pagina \" counter(page) \" de \" counter(pages); }}");
		sb.append("table {width: 100%;border-spacing: 0px;border-bottom: none; font-family: serif; font-size: 12px;}");
		sb.append("caption {height: 30px;font-size: 14px;font-weight: bold;}");
		sb.append("thead tr {height: 30px;vertical-align: top; text-align: left; text-transform: uppercase;font-weight: bold;}");
		sb.append("tfoot tr {height: 30px;vertical-align: bottom; text-transform: uppercase;font-weight: bold;}");
		sb.append("tbody tr {height: 20px;vertical-align: middle;}");
		sb.append(".nobreak {page-break-inside: avoid;}");
		sb.append("</style>");
		sb.append("<title>" + titulo + "</title></head>");
		sb.append("<body>");
		return sb.toString();
	}

	/**
	 * Metodo que gera o cabecalho da listagem.
	 * 
	 * @return o cabecalho da listagem.
	 */
	public String getCabecalhoListagem() {
		StringBuffer sb = new StringBuffer("<caption>:: " + expLista.getNome() + " ::</caption><thead>");
		for (ExpMeta meta : expLista.getMetadados()) {
			if (meta != null) {
				sb.append("<th style='width:" + (meta.getTamanho() + 5) + "px'><div class=\"nobreak\">" + meta.getRotulo() + "</div></th>");
			}
		}
		sb.append("</thead>");
		return sb.toString();
	}

	/**
	 * Metodo que gera o corpo da listagem.
	 * 
	 * @return o corpo da listagem.
	 */
	public String getCorpoListagem(int fim) {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < fim; j++) {
			sb.append("<tr>");
			for (int i = 0; i < expLista.getMetadados().size(); i++) {
				ExpMeta meta = expLista.getMetadados().get(i);
				if (meta != null) {
					sb.append("<td><div class=\"nobreak\">" + getValor(lista.getDados()[j][i]) + "</div></td>");

					if (meta.getGrupo() != null) {
						double valor = Double.valueOf(lista.getDados()[j][i]);
						switch (meta.getGrupo()) {
						case CONTAGEM:
							agrupados[i]++;
							break;
						case MAXIMO:
							agrupados[i] = agrupados[i] > valor ? agrupados[i] : valor;
							break;
						case MINIMO:
							agrupados[i] = agrupados[i] < valor ? agrupados[i] : valor;
							break;
						case SOMA:
							agrupados[i] += valor;
							break;
						case MEDIA:
							agrupados[i] += valor / fim;
							break;
						}
					}
				} else {
					agrupados[i] = Double.MIN_NORMAL;
				}
			}
			sb.append("</tr>");
			regs++;
		}
		return sb.toString();
	}

	/**
	 * Metodo que gera o rodape da listagem.
	 * 
	 * @return o rodape da listagem.
	 */
	public String getRodapeListagem() {
		boolean semGrupo = true;
		StringBuffer rodape = new StringBuffer();

		for (int i = 0; i < agrupados.length; i++) {
			if (agrupados[i] == 0) {
				rodape.append("<td>&nbsp;</td>");
			} else if (agrupados[i] > Double.MIN_NORMAL) {
				semGrupo = false;
				rodape.append("<td><div class=\"nobreak\">").append(UtilServer.formataNumero(agrupados[i], 1, 2, true)).append("</div></td>");
			}
		}

		StringBuffer sb = new StringBuffer("<tfoot>");
		if (!semGrupo) {
			sb.append("<tr><td colspan='").append(agrupados.length).append("'><div class=\"nobreak\">").append(auth.getConf().get("txtTotal")).append("<hr /></div></td></tr>");
			sb.append("<tr>").append(rodape.toString()).append("</tr>");
		}
		sb.append("<tr><td colspan='").append(agrupados.length).append("'><div class=\"nobreak\">").append(auth.getConf().get("txtRegistro")).append(" :: ").append(regs)
				.append("</div></td></tr></tfoot>");

		return sb.toString();
	}
}
