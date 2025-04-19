
import { useState, ChangeEvent, useRef } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Search } from "lucide-react";
import Papa from "papaparse";

interface ContractData {
  Contract: string;
  "Contract Account": string;
  IBCName: string;
  Portfolio: string;
  "CB Offer": string;
  "Rebate Offer": string;
  "PWO Scheme": string;
  [key: string]: string; // Index signature for dynamic access
}

const Index = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [contracts, setContracts] = useState<ContractData[]>([]);
  const [filteredContracts, setFilteredContracts] = useState<ContractData[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState("");
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleSearch = () => {
    if (!searchTerm.trim()) {
      setFilteredContracts([]);
      return;
    }
    
    setIsLoading(true);
    
    const results = contracts.filter(
      contract => 
        contract.Contract.toLowerCase().includes(searchTerm.toLowerCase()) || 
        contract["Contract Account"].toLowerCase().includes(searchTerm.toLowerCase())
    );
    
    setFilteredContracts(results);
    setIsLoading(false);
    
    if (results.length === 0) {
      setMessage("No contracts found matching your search criteria.");
    } else {
      setMessage(`Found ${results.length} matching contracts.`);
    }
  };

  const handleFileUpload = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    
    setIsLoading(true);
    setMessage("Processing CSV file...");
    
    Papa.parse(file, {
      header: true,
      skipEmptyLines: true,
      complete: (results) => {
        const data = results.data as ContractData[];
        setContracts(data);
        setMessage(`Successfully loaded ${data.length} contracts. Ready to search.`);
        setIsLoading(false);
      },
      error: (error) => {
        setMessage(`Error parsing CSV: ${error.message}`);
        setIsLoading(false);
      }
    });
    
    // Reset file input
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-gradient-to-r from-blue-700 to-blue-900 text-white p-6 shadow-md">
        <div className="max-w-7xl mx-auto">
          <h1 className="text-3xl font-bold">Contract Finder Nexus</h1>
          <p className="text-blue-100 mt-2">Search contract details efficiently</p>
        </div>
      </header>
      
      <main className="max-w-7xl mx-auto p-4 md:p-6">
        <div className="grid gap-6 md:grid-cols-12">
          {/* Upload Section */}
          <Card className="md:col-span-4">
            <CardHeader>
              <CardTitle>Import CSV Data</CardTitle>
              <CardDescription>
                Upload a CSV file with contract data
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <Input 
                  type="file" 
                  accept=".csv" 
                  onChange={handleFileUpload} 
                  ref={fileInputRef}
                  className="file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                />
                <p className="text-sm text-gray-500">
                  {contracts.length > 0 ? 
                    `${contracts.length} contracts loaded.` : 
                    "No data loaded yet. Please upload a CSV file."}
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Search Section */}
          <Card className="md:col-span-8">
            <CardHeader>
              <CardTitle>Search Contracts</CardTitle>
              <CardDescription>
                Enter a Contract ID or Account number
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex space-x-2">
                <Input
                  type="text"
                  placeholder="Enter Contract ID or Contract Account"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                  className="flex-1"
                  disabled={contracts.length === 0}
                />
                <Button 
                  onClick={handleSearch} 
                  disabled={contracts.length === 0 || isLoading}
                >
                  <Search className="mr-2 h-4 w-4" /> Search
                </Button>
              </div>
              {message && <p className="mt-2 text-sm text-gray-600">{message}</p>}
            </CardContent>
          </Card>
        </div>

        {/* Results Table */}
        {filteredContracts.length > 0 && (
          <Card className="mt-6">
            <CardHeader>
              <CardTitle>Search Results</CardTitle>
              <CardDescription>{filteredContracts.length} contracts found</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="rounded-md border overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Contract</TableHead>
                      <TableHead>Contract Account</TableHead>
                      <TableHead>IBC Name</TableHead>
                      <TableHead>Portfolio</TableHead>
                      <TableHead>CB Offer</TableHead>
                      <TableHead>Rebate Offer</TableHead>
                      <TableHead>PWO Scheme</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredContracts.map((contract, index) => (
                      <TableRow key={index}>
                        <TableCell>{contract.Contract}</TableCell>
                        <TableCell>{contract["Contract Account"]}</TableCell>
                        <TableCell>{contract.IBCName}</TableCell>
                        <TableCell>{contract.Portfolio}</TableCell>
                        <TableCell>{contract["CB Offer"]}</TableCell>
                        <TableCell>{contract["Rebate Offer"]}</TableCell>
                        <TableCell>{contract["PWO Scheme"]}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        )}
      </main>
      
      <footer className="mt-8 py-6 bg-gray-100 border-t">
        <div className="max-w-7xl mx-auto px-4 text-center text-gray-500 text-sm">
          Contract Finder Nexus App — Optimized for large datasets
        </div>
      </footer>
    </div>
  );
};

export default Index;
