# Bank Account Reputation Stub

## Summary

This uk.gov.hmrc.bars.stub will provide you with an implementation for all the non-deprecated BARS endpoints.

The supported endpoints are:

* `/verify/personal` - For verifying given sort code/account number/personal details
* `/verify/business` - For verifying given sort code/account number/business details
* `/validate/bank-details` - For performing a modulus check and collecting branch information for a sort code/account number combination
* `/metadata/:sc` - For displaying metadata information for a branch (identified by sort code)

This mock will mirror the functionality in the Bank Account Reputation Service (BARS).  To integrate with these endpoints you should refer to the [BARS documentation](https://github.com/hmrc/bank-account-reputation/blob/master/docs/README.md).

****Note*** * The `/verify/*` endpoints will tell you if a name has been partially matched.  To replicate this behaviour any substring of the name associated with an account below will return `nameMatches: partial`.  As an example if you find the name `Adena Shuman` in the data below, sending over `Adena` will return `nameMatches: partial`.  This is dumb behaviour, so it's worth noting that send over `a` will also return `nameMatches: partial` as it's a simple substring match and the name `Adena Shuman` contains the letter `a`. 

If you have any queries relating to this test data, come and have a chat with us in [#team-cip-attribute-validation](https://hmrcdigital.slack.com/archives/team-cip-attribute-validation)

## Starting the uk.gov.hmrc.bars.stub

The uk.gov.hmrc.bars.stub can be started up using service manager with the following command:

```sm --start BANK_ACCOUNT_REPUTATION_STUB -r --port 9999```

# Available test data

## Sort code test data

| Sort code |Roll number required|BACS support|CHAPS support|Requires paper direct debit instruction|Supports direct debit|Supports direct credit (standard bank transfer)| Comments                                                       |
|-----------| --- | --- | --- | --- | --- | --- |----------------------------------------------------------------|
| 206705    |No|Scheme member|Indirect support| No | No | No |                                                                |
| 609593    |Yes|Sponsored institution|Not set up| No | No | Yes |                                                                |
| 207102    |No|Sponsored institution|Indirect support| No | Yes | No |                                                                |
| 207106    |No|Scheme member|Direct support| Yes | Yes | Yes |                                                                |
| 406252    |No|Scheme member|Indirect support| No | No | No |                                                                |
| 608369    |Yes|Sponsored institution|Not set up| No | No | Yes |                                                                |
| 405125    |No|Sponsored institution|Indirect support| No | Yes | No |                                                                |
| 301658    |No|Scheme member|Direct support| Yes | Yes | Yes |                                                                |
| 309598    |No|Not set up|Not set up| N/A | No | No | Any 8 digit number will return the same response               |
| 201147    |N/A|N/A|N/A| N/A | N/A | N/A | On deny list, any 8 digit number will return the same response |
| 309696    |N/A|N/A|N/A| N/A | N/A | N/A | Sort code is not on EISCD                                      |

Select the sort code from the above list that provides the Direct Debit/Direct Credit response you desire and then search for the associated account number in the tables below.

## Personal account test data

| Sort code | Account number | Name | Valid         | Exists | Address Match | Non Consented | Deceased |
|-----------|----------------| --- |---------------| --- | --- | --- | --- |
| 206705    | 44311611       | Marceline Foret | Yes           | Yes | Indeterminate | Indeterminate | Indeterminate |
| 206705    | 44333611       | N/A | Yes           | No | Indeterminate | Indeterminate | Indeterminate |
| 206705    | 44344611       | Alex Askew | Yes           | Yes | Yes | No | No |
| 206705    | 44355611       | Patrick O'Connor | Yes           | Yes | Yes | No | Yes |
| 206705    | 44366611       | Autumn Carrier | Yes           | Yes | Yes | Yes | No |
| 206705    | 44377611       | Adena Shuman | Yes           | Yes | Yes | Yes | Yes |
| 206705    | 44388611       | N/A | Yes           | Indeterminate | No | Yes | Indeterminate | No |
| 206705    | 44399611       | N/A | Yes           | No | No | No | No | No |
| 207102    | 44311655       | Teddy Dickson | Yes           | Yes | Indeterminate | Indeterminate | Indeterminate |
| 207102    | 44333655       | N/A | Yes           | No | Indeterminate | Indeterminate | Indeterminate |
| 207102    | 44344655       | Zula Mahoney | Yes           | Yes | Yes | No | No |
| 207102    | 44355655       | Eleonora Marlow | Yes           | Yes | Yes | No | Yes |
| 207102    | 44366655       | Lane Damon | Yes           | Yes | Yes | Yes | No |
| 207102    | 44377655       | Jina Vance | Yes           | Yes | Yes | Yes | Yes |
| 207102    | 44388655       | N/A | Yes           | Indeterminate | No | Yes | Indeterminate | No |
| 207102    | 44399655       | N/A | Yes           | No | No | No | No | No |
| 207106    | 44311677       | Melvin Loper | Yes           | Yes | Indeterminate | Indeterminate | Indeterminate |
| 207106    | 44333677       | N/A | Yes           | No | Indeterminate | Indeterminate | Indeterminate |
| 207106    | 44344677       | Felipa Doherty | Yes           | Yes | Yes | No | No |
| 207106    | 44355677       | Emiko Snowden | Yes           | Yes | Yes | No | Yes |
| 207106    | 44366677       | Cecila Angulo | Yes           | Yes | Yes | Yes | No |
| 207106    | 44377677       | Xenia Barth | Yes           | Yes | Yes | Yes | Yes |
| 207106    | 44388677       | N/A | Yes           | Indeterminate | No | Yes | Indeterminate | No |
| 207106    | 44399677       | N/A | Yes           | No | No | No | No | No |
| 609593    | 95311500       | Angelique Whitney | Yes           | Yes | Indeterminate | Indeterminate | Indeterminate |
| 609593    | 91771500       | N/A | Yes           | No | Indeterminate | Indeterminate | Indeterminate |
| 609593    | 91881500       | Leola Fairchild | Yes           | Yes | Yes | No | No |
| 609593    | 91991500       | Dannielle Winfield-Jones | Yes           | Yes | Yes | No | Yes |
| 609593    | 91661500       | Tinisha Bussey | Yes           | Yes | Yes | Yes | No |
| 609593    | 91551500       | Andre Pèna | Yes           | Yes | Yes | Yes | Yes |
| 609593    | 91441500       | N/A | Yes           | Indeterminate | No | Yes | Indeterminate | No |
| 609593    | 91331500       | N/A | Yes           | No | No | No | No | No |
| 406252    | 54344611       | Rory Schroeder | Indeterminate | Yes | Yes | No | No |
| 406252    | 54355611       | Eldred O'Connor | Indeterminate | Yes | Yes | No | Yes |
| 406252    | 54366611       | Bridgette Weber | Indeterminate | Yes | Yes | Yes | No |
| 406252    | 54377611       | Reyes Casper | Indeterminate | Yes | Yes | Yes | Yes |
| 406252    | 54388611       | N/A | Indeterminate | Indeterminate | Yes | Indeterminate | No |
| 406252    | 54399611       | N/A | Indeterminate | No | No | No | No |
| 608369    | 54344655       | Alba Murazik | Indeterminate | Yes | Yes | No | No |
| 608369    | 54355655       | Manley Renner | Indeterminate | Yes | Yes | No | Yes |
| 608369    | 54366655       | Lincoln Mills | Indeterminate | Yes | Yes | Yes | No |
| 608369    | 54377655       | Judson Moen | Indeterminate | Yes | Yes | Yes | Yes |
| 608369    | 54388655       | N/A | Indeterminate | Indeterminate | Yes | Indeterminate | No |
| 608369    | 54399655       | N/A | Indeterminate | No | No | No | No |
| 405125    | 54344677       | Casandra Wilkinson | Indeterminate | Yes | Yes | No | No |
| 405125    | 54355677       | Brayan Macejkovic | Indeterminate | Yes | Yes | No | Yes |
| 405125    | 54366677       | Woodrow Kassulke | Indeterminate | Yes | Yes | Yes | No |
| 405125    | 54377677       | Nichole Cartwright | Indeterminate | Yes | Yes | Yes | Yes |
| 405125    | 54388677       | N/A | Indeterminate | Indeterminate | Yes | Indeterminate | No |
| 405125    | 54399677       | N/A | Indeterminate | No | No | No | No |
| 301658    | 01881500       | Bonita Streich | Indeterminate | Yes | Yes | No | No |
| 301658    | 01991500       | Alek Johnson | Indeterminate | Yes | Yes | No | Yes |
| 301658    | 01661500       | Edmond Miller | Indeterminate | Yes | Yes | Yes | No |
| 301658    | 01551500       | Cordell Roob | Indeterminate | Yes | Yes | Yes | Yes |
| 222222    | 54648979       | N/A | Indeterminate | Inapplicable | Inapplicable | Inapplicable | Inapplicable | Inapplicable |
| 207106    | 11111111       | N/A | No             | Inapplicable | Inapplicable | Inapplicable | Inapplicable | Inapplicable |

## Business account test data

| Sort code | Account number | Company Name | Valid         | Exists | Postcode Match | Company Registration Number Match |
|-----------|----------------| --- |---------------| --- | --- | --- |
| 206705    | 86473611       | Epic Adventure Inc | Yes           | Yes | Yes | Yes |
| 206705    | 86563611       | Sanguine Skincare | Yes           | Yes | Yes | No |
| 206705    | 76523611       | Vortex Solar | Yes           | Yes | No | Yes |
| 206705    | 56523611       | Innovation Arch | Yes           | Yes | No | No |
| 206705    | 56945688       | Eco Focus | Yes           | No | No | No |
| 207102    | 86473611       | Flux Water Gear | Yes           | Yes | Yes | Yes |
| 207102    | 86563611       | Lambent Illumination | Yes           | Yes | Yes | No |
| 207102    | 76523611       | Boneféte Fun | Yes           | Yes | No | Yes |
| 207102    | 56523611       | Cogent-Data | Yes           | Yes | No | No |
| 207102    | 74597611       | Cipher Publishing | Yes           | No | No | No |
| 207106    | 86473611       | Security Engima | Yes           | Yes | Yes | Yes |
| 207106    | 86563611       | Megacorp | Yes           | Yes | Yes | No |
| 207106    | 76523611       | Genomics Inc | Yes           | Yes | No | Yes |
| 207106    | 56523611       | Full Force Futures | Yes           | Yes | No | No |
| 207106    | 74597611       | Resource Refresh | Yes           | No | No | No |
| 609593    | 96863604       | O'Connor Construction | Yes           | Yes | Yes | Yes |
| 609593    | 96113600       | Candyland Consulting | Yes           | Yes | Yes | No |
| 609593    | 96223600       | PLASTIC PACKAGING INC | Yes           | Yes | No | Yes |
| 609593    | 96443600       | Mercury Management Consultants | Yes           | Yes | No | No |
| 609593    | 96883600       | Essex Skips | Yes           | No | No | No |
| 406252    | 96473611       | Waite Furnishings Ltd | Indeterminate | Yes | Yes | Yes |
| 406252    | 96563611       | Hackman Cash & Carry Ltd | Indeterminate | Yes | Yes | No |
| 406252    | 86523611       | Abraham Fitness Products Ltd | Indeterminate | Yes | No | Yes |
| 406252    | 66523611       | Yeager Boiler Repairs Ltd | Indeterminate | Yes | No | No |
| 406252    | 66945688       | Yeoman Refrigeration Ltd | Indeterminate | No | No | No |
| 608369    | 96473611       | Archer Foods Ltd | Indeterminate | Yes | Yes | Yes |
| 608369    | 96563611       | Jeffries Motorcycles| Indeterminate | Yes | Yes | No |
| 608369    | 86523611       | Samson Technical Services | Indeterminate | Yes | No | Yes |
| 608369    | 66523611       | Gatley Footwear | Indeterminate | Yes | No | No |
| 608369    | 84597611       | Quinn Reproductions | Indeterminate | No | No | No |
| 405125    | 96473611       | Zanetti Office Supplies | Indeterminate | Yes | Yes | Yes |
| 405125    | 96563611       | Francis Cabs Ltd | Indeterminate | Yes | Yes | No |
| 405125    | 86523611       | Cunningham Coaches | Indeterminate | Yes | No | Yes |
| 405125    | 66523611       | Whittaker Care Services | Indeterminate | Yes | No | No |
| 405125    | 84597611       | Shining Car Supplies | Indeterminate | No | No | No |
| 301658    | 06863604       | Build Me Up | Indeterminate | Yes | Yes | Yes |
| 301658    | 06113600       | Flowquest | Indeterminate | Yes | Yes | No |
| 301658    | 06223600       | Electrocloud | Indeterminate | Yes | No | Yes |
| 301658    | 06443600       | Onyx Construction Group | Indeterminate | Yes | No | No |
| 301658    | 06883600       | Innovize | Indeterminate | No | No | No |
| 222222    | 76523611       | N/A | Indeterminate | Inapplicable | Inapplicable | Inapplicable |
| 207106    | 111111         | N/A | No            | Inapplicable | Inapplicable | Inapplicable |
