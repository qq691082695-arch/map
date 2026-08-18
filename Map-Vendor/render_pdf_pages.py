import pypdfium2 as pdfium
from PIL import Image, ImageDraw
from pathlib import Path

pdf=Path(r"D:\Map-Vendor\.codex\prd_render\prd.pdf")
out=pdf.parent
d=pdfium.PdfDocument(str(pdf))
thumbs=[]
for i,p in enumerate(d):
    im=p.render(scale=1.35).to_pil().convert("RGB")
    path=out/f"page-{i+1}.png"; im.save(path)
    tw=300; th=int(im.height*tw/im.width)
    im=im.resize((tw,th))
    canvas=Image.new("RGB",(tw,th+24),"white"); canvas.paste(im,(0,24))
    ImageDraw.Draw(canvas).text((5,5),f"Page {i+1}",fill="black")
    thumbs.append(canvas)
for batch in range((len(thumbs)+5)//6):
    arr=thumbs[batch*6:(batch+1)*6]
    sheet=Image.new("RGB",(600,((len(arr)+1)//2)*len(arr[0].getbands())*0+((len(arr)+1)//2)*(arr[0].height+10)),"#aaaaaa")
    sheet=Image.new("RGB",(620,((len(arr)+1)//2)*(arr[0].height+10)+10),"#aaaaaa")
    for j,im in enumerate(arr): sheet.paste(im,(10+(j%2)*305,10+(j//2)*(im.height+10)))
    sheet.save(out/f"contact-{batch+1}.png")
print(len(d))
