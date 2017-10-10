package pkcs;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.dtools.ini.BasicIniFile;
import org.dtools.ini.IniFile;
import org.dtools.ini.IniFileReader;
import org.dtools.ini.IniItem;
import org.dtools.ini.IniSection;

import Utils.MethodResult;
import Utils.Pkcs11;
import sun.security.pkcs11.wrapper.PKCS11Exception;
/**
 * CAsocket�Լ�CA��ȡ
 * @author shotacon
 *
 */
public class GetUser {

	/**
	 * ��ȡCA֤���е��û���
	 * 
	 * @return
	 */
	private static MethodResult getUser() {
		// getMechanismInfo();
		MethodResult result = new MethodResult();
		try {
			Pkcs11.getPKCS11();
			List<X509Certificate> certificate = Pkcs11.getCertificate();
			X509Certificate cert = certificate.get(0);
			Principal subjectDN = cert.getSubjectDN();
			// cert.checkValidity(new Date());
			// CN=����, T=650108198311111111, C=CN
			String name = subjectDN.getName();
			String[] split = name.split(",");
			result.setObject(split[0].split("=")[1] + "-" + split[1].split("=")[1]);
			result.setResult(1);
			result.setResultMsg("��ȡ�ɹ�");
			return result;
		} catch (CertificateException e) {
			e.printStackTrace();
			result.setResult(0);
			result.setResultMsg("��ȡ֤��ʧ�ܻ�֤�鳬����Ч��!");
			return result;
		} catch (PKCS11Exception e) {
			e.printStackTrace();
			result.setResult(0);
			result.setResultMsg("��ȡӲ���쳣!����UKey!");
			return result;
		}

		// boolean flag=checkPasswd("1234".toCharArray());
		// System.out.println("PIN����֤�����"+flag);
		// pair(pkcs11,session);
		// System.out.print("ǩ�����ݣ�");
		// for(int i=0;i<plianText.length;i++)
		// System.out.print(String.format("%02X ",plianText[i]));
		// System.out.println("");
		// byte [] sigmsg=sign(plianText);
		// System.out.println("\nǩ�������"+Base64.encode(sigmsg));
	}

	/**
	 * swingչʾ
	 */
	private static void createAndShowGUI() {
		String text = "";
		JFrame frame = new JFrame("CA֤���ȡ");
		JLabel label = null;
		boolean isshow = false;
		MethodResult result = getUser();
		try {
			// ȷ��һ��Ư������۷��
			JFrame.setDefaultLookAndFeelDecorated(true);
			// ���������ô���
			frame.setPreferredSize(new Dimension(300, 200));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setResizable(false);
			frame.setLocationRelativeTo(null);
			frame.setUndecorated(true);
			// JPanel panel = new JPanel();
			// // ������
			// frame.add(panel);
			// JButton closeButton = new JButton("�ر�");
			// closeButton.setBounds(10, 80, 80, 25);
			// panel.add(closeButton);
			// panel.setVisible(true);
			// JButton loginButton = new JButton("login");
			// loginButton.setBounds(10, 80, 80, 25);
			// frame.add(loginButton);
			if (result.getResult() == 0) {
				label = new JLabel("��ѽ :" + result.getResultMsg());
				frame.getContentPane().add(label);
				isshow = true;
			} else {
				text = (String) result.getObject();
			}
			Writer w = new FileWriter("C:/Users/shotacon/Desktop/NewReadMe.txt");
			BufferedWriter buffWriter = new BufferedWriter(w);
			buffWriter.write(text);
			buffWriter.close();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			label = new JLabel("Error :" + e.getMessage());
			frame.getContentPane().add(label);
			isshow = true;
		}
		label.setBounds(10, 20, 80, 25);
		// panel.add(label);
		// ��ʾ����
		frame.pack();
		frame.setVisible(true);
		System.out.println("д��ɹ���");
		if (!isshow) {
			// System.exit(0);
		}
	}

	/**
	 * socket����
	 * 
	 * @throws IOException
	 */
	private static void socketServer() throws IOException {
		int port = Integer.parseInt("9003");
		try {
			IniFile iniFile = new BasicIniFile();
			System.out.println(new File("").getCanonicalPath());
			File file = new File(new File("").getAbsolutePath() + "/CaConfig.ini");
			IniFileReader rad = new IniFileReader(iniFile, file);
			rad.read();
			IniSection iniSection = iniFile.getSection("caport");
			IniItem iniItem = iniSection.getItem("port");
			String name = iniItem.getValue();
			port = Integer.parseInt(name);
		} catch (Exception e) {
			System.out.println("INI�ļ���ȡ�쳣");
		}

		ServerSocket server = new ServerSocket(port);
		Socket socket;
		String line;
		BufferedReader br;
		BufferedWriter ps;
		System.out.println("������������");
		do {
			socket = server.accept();

			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			line = br.readLine();
			System.out.println("�ͻ��ˣ�" + line + "\n");
			if (line == null)
				continue;
			if (line.equals("EXIT")) {
				server.close();
				System.out.println("���񽫹رգ�");
				ps = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				ps.write("���񽫹رգ�");
				// try {
				// Thread.sleep(5000);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// ps.write("socket�ر��쳣" + e.getMessage());
				// }
				break;
			} else if (line.equals("DOWNLINE")) {
				// ��ʾӦ�� GUI
				// javax.swing.SwingUtilities.invokeLater(new Runnable() {
				// public void run() {
				// createAndShowGUI();
				// }
				// });
				MethodResult result = getUser();
				FileWriter fw = null;
				try {
				//����ļ����ڣ���׷�����ݣ�����ļ������ڣ��򴴽��ļ�
				File f=new File(new File("").getAbsolutePath()+"\\CAlog.txt");
				fw = new FileWriter(f, true);
				} catch (IOException e) {
				e.printStackTrace();
				}
				PrintWriter pw = new PrintWriter(fw);
				pw.println(result.getResultMsg());
				pw.println((String) result.getObject());
				pw.flush();
				try {
				fw.flush();
				pw.close();
				fw.close();
				} catch (IOException e) {
				e.printStackTrace();
				}
				ps = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				if (result.getResult() == 0) {
					ps.write("��ѽ :" + result.getResultMsg());
					System.out.println("��ѽ :" + result.getResultMsg());
				} else {
					ps.write("success:" + (String) result.getObject());
					System.out.println((String) result.getObject());
				}
				ps.flush();
				System.out.println("�ѷ��ؽ����");
				ps.close();
				socket.close();
			}
			br.close();
		} while (true);
		socket.close();
	}

	/**
	 * ������
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// ����socket����
		try {
			socketServer();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("socket������ʧ�ܣ�");
		}
	}
}
