package data.transporter.core;

import com.alchemyapi.api.AlchemyAPI;

import Util.SentimentExtractor;
import Util.SimilarityCalculator;
import blogs.AFieldOfViewsBlog;
import blogs.AndreasSpeck;
import blogs.MaikelNabil;
import blogs.NoBordersard;
import blogs.NoMilService;
import blogs.PlataformaOtan;
import blogs.Sinkuartel;
import blogs.Tarcoteca;
import blogs.TarcotecaCounterInfo;
import blogs.Tgnapau;
import blogs.Wri;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub	
		
		try
		{
		/*
		AFieldOfViewsBlog afvb = new AFieldOfViewsBlog();
		afvb.run();
		*/
		/*
		AndreasSpeck as = new AndreasSpeck();
		as.run();
		*/
		/*
		MaikelNabil mn = new MaikelNabil();
		mn.run();
		*/
		/*
		NoBordersard nb = new NoBordersard();
		nb.run();
		*/
		/*
		NoMilService nms = new NoMilService();
		nms.run();
		*/
		/*
		Tgnapau tg = new Tgnapau();
		tg.run();
		*/
		/*
		Sinkuartel sktl = new Sinkuartel();
		sktl.run();
		*/
		/*
		Tarcoteca tct = new Tarcoteca();
		tct.run();
		*/
		/*
		TarcotecaCounterInfo t = new TarcotecaCounterInfo();
		t.run();
		*/
		/*
		Wri wri = new Wri();
		wri.run();
		*/
		/*
		PlataformaOtan po = new PlataformaOtan();
		po.run();
		*/
		
		SentimentExtractor se = new SentimentExtractor();
		AlchemyAPI alchemyObj = AlchemyAPI.GetInstanceFromFile("api_key.txt");
		se.run(alchemyObj);
		
		/*
		 * TFIDF --> code incomplete - needs to complete before SC can run
		SimilarityCalculator sc = new SimilarityCalculator();
		sc.run(false);
		*/
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
