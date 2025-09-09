Hello valentin.
Szia



Sql tablak.

Users
	Data:
Username
Full name
jelszo
Role	employee/admin/superadmin
id
Addresses
Name index+search
Address name  index+search


Felvonok
Felvono jelzese
Zipcode
Worksheets
Id
Creation date
Erkezes date
Leaving date
Sorszam: 2025-02-25/001
Pdf elerresi ut
User who created it: id
Address id
Customer id
Munka tipusa javitas/karbantartas/egyeb
Kiszallas tipusa: normal/ejszakai/hetvege-unnepnap
Munka leirasa 
Anyagfelhasznalas vektor [{anyagfelhasznalas neve + anyagfelhasznalas mennyisege 20db 30m 40l}]
megjegyzes/comment

Intervallum szamitasa erkezes+leaving date orara mindig felfele kerekitve
Azaz m unkavegzes ideje, ezt nem kell menteni, ezt csak pdf-en kell valahol.
Munkalap pdf neve -> munkatipusa -> j + date k + date e +date + sorszam
                                                             j2025-02-25/001.pdf


Settings 
Logo amit munkalapra teszunk
Munkalap alja szoveg


Project description: 
We have two roles in our system: The first role is called employee The second role is called admin. The employee is able to add new worksheets And he or she can edit those he or she created. The admin is able to create new addresses and edit them. He is also able to edit any worksheet. The admin is able to modify the settings, so modify the logo which we put on the worksheet, also the text which we will see on the bottom of the worksheet. The worksheet is able to generate PDF The PDF should contain the creation date And the PDF should be able to be signed. We need a button to be able to download the PDF. We should store the PDF on the server and put a link onto the worksheet items in the table to have the path to the PDF. 




