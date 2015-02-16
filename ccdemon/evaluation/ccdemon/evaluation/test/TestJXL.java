package ccdemon.evaluation.test;

import java.io.File;
import jxl.*;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class TestJXL{
    public static void main(String[] args) {
        //������
        String title[]={"��ɫ","���","��������","��������"};
        //����
        String context[][]={{"UC11","���ÿγ�","�����γ�"},
                            {"UC12","����ѧ������","������γ̹�����ѧ������"},
                            {"UC21","�鿴ѧ������",""},
                            {"UC22","�鿴С����Ϣ","��ʾ�����������С���б���Ϣ"}
                            };
        //����ִ��
        try { 
            //t.xlsΪҪ�½����ļ���
            WritableWorkbook book= Workbook.createWorkbook(new File("t.xls")); 
            //������Ϊ����һҳ���Ĺ���������0��ʾ���ǵ�һҳ 
            WritableSheet sheet=book.createSheet("��һҳ",0); 
            
            //д������
            for(int i=0;i<4;i++)    //title
                sheet.addCell(new Label(i,0,title[i])); 
            for(int i=0;i<4;i++)    //context
            {
                for(int j=0;j<3;j++)
                {
                    sheet.addCell(new Label(j+1,i+1,context[i][j])); 
                }
            }
            sheet.addCell(new Label(0,1,"��ʦ"));
            sheet.addCell(new Label(0,3,"����"));
            
            /*�ϲ���Ԫ��.�ϲ��ȿ����Ǻ���ģ�Ҳ�����������
             *WritableSheet.mergeCells(int m,int n,int p,int q);   ��ʾ��(m,n)��(p,q)�ĵ�Ԫ����ɵľ�������ϲ�
             * */
            sheet.mergeCells(0,1,0,2);
            sheet.mergeCells(0,3,0,4);
            
            //д������
            book.write(); 
            //�ر��ļ�
            book.close(); 
        }
        catch(Exception e) { } 
    }
}
